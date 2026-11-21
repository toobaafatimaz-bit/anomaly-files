package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AnomalyAudioEngine
import com.example.data.cases.CaseCatalog
import com.example.data.cases.CaseCatalogPart2
import com.example.data.local.*
import com.example.data.repository.GameRepository
import com.example.engine.FirstPersonEngine
import com.example.engine.Player3DState
import com.example.engine.RuntimeEntity
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class ActiveInvestigationState(
    val caseDef: CaseDefinition,
    val gameMode: GameMode,
    val challengeModifier: ChallengeModifier? = null,
    val player: Player3DState = Player3DState(),
    val openDoors: Set<Pair<Int, Int>> = emptySet(),
    val entities: List<RuntimeEntity> = emptyList(),
    val activeEquipment: EquipmentType? = null,
    val batteryPercent: Float = 100f,
    val healthSanityPercent: Float = 100f,
    val elapsedSeconds: Int = 0,
    val timeRemainingSeconds: Int? = null,
    val scannerPulseProgress: Float = 0f,
    val emfStrength: Float = 0.1f,
    val lookTarget: InteractiveObject? = null,
    val inspectedObject: InteractiveObject? = null,
    val adaptation: AdaptationState = AdaptationState(),
    val hudBannerMessage: String? = null,
    val cameraFlashAlpha: Float = 0f,
    val animTimeSec: Float = 0f,
    val isPaused: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AnomalyDatabase.getInstance(application)
    private val repository = GameRepository(db.anomalyDao())
    val audioEngine = AnomalyAudioEngine()

    val profileState: StateFlow<PlayerProfileEntity> = repository.profileFlow
        .onEach { audioEngine.isEnabled = it.soundEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfileEntity())

    val caseProgressState: StateFlow<Map<String, CaseProgressEntity>> = repository.caseProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allCollectedEvidence: StateFlow<List<CollectedEvidenceEntity>> = repository.allEvidenceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedAchievements: StateFlow<Set<String>> = repository.unlockedAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _activeInvestigation = MutableStateFlow<ActiveInvestigationState?>(null)
    val activeInvestigation: StateFlow<ActiveInvestigationState?> = _activeInvestigation.asStateFlow()

    private val _lastEvaluation = MutableStateFlow<CaseEvaluationResult?>(null)
    val lastEvaluation: StateFlow<CaseEvaluationResult?> = _lastEvaluation.asStateFlow()

    private var loopJob: Job? = null
    private var moveInputX: Float = 0f // strafe
    private var moveInputY: Float = 0f // forward/back
    private var stepAudioCooldown = 0f
    private var emfTickCooldown = 0f
    private var secondAccumulator = 0f
    private val visitedZoneSet = mutableMapOf<Pair<Int, Int>, Int>()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun observeCaseEvidence(caseId: String): Flow<List<CollectedEvidenceEntity>> =
        repository.observeCaseEvidence(caseId)

    fun observeBoardLinks(caseId: String): Flow<List<BoardLinkEntity>> =
        repository.observeBoardLinks(caseId)

    fun completeIntro() {
        viewModelScope.launch {
            repository.updateProfile { it.copy(introCompleted = true) }
        }
    }

    fun markPostCase001TwistSeen() {
        viewModelScope.launch {
            repository.updateProfile { it.copy(postCase001TwistSeen = true) }
            repository.unlockAchievement("ach_fifth_figure")
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            stopInvestigationLoop()
            _activeInvestigation.value = null
            _lastEvaluation.value = null
            repository.resetForNewGame()
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(difficulty = difficulty.name) }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        audioEngine.isEnabled = enabled
        viewModelScope.launch {
            repository.updateProfile { it.copy(soundEnabled = enabled) }
        }
    }

    fun setLookSensitivity(value: Float) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(lookSensitivity = value.coerceIn(0.4f, 2.2f)) }
        }
    }

    fun selectOutfit(outfitName: String) {
        viewModelScope.launch {
            repository.updateProfile { it.copy(activeOutfit = outfitName) }
        }
    }

    fun purchaseEquipment(equipment: EquipmentType) {
        viewModelScope.launch {
            val profile = profileState.value
            val unlocked = profile.unlockedEquipmentCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (equipment.name in unlocked || profile.credits < equipment.unlockCost) return@launch
            unlocked.add(equipment.name)
            repository.updateProfile {
                it.copy(
                    credits = it.credits - equipment.unlockCost,
                    unlockedEquipmentCsv = unlocked.joinToString(",")
                )
            }
            audioEngine.playEvidenceCollected()
        }
    }

    fun upgradeBattery() {
        viewModelScope.launch {
            val profile = profileState.value
            val cost = profile.batteryUpgradeLevel * 100
            if (profile.batteryUpgradeLevel >= 4 || profile.credits < cost) return@launch
            repository.updateProfile {
                it.copy(
                    credits = it.credits - cost,
                    batteryUpgradeLevel = it.batteryUpgradeLevel + 1
                )
            }
            audioEngine.playEvidenceCollected()
        }
    }

    fun beginCase(
        caseDef: CaseDefinition,
        mode: GameMode,
        modifier: ChallengeModifier? = null,
        resetPreviousEvidence: Boolean = false
    ) {
        viewModelScope.launch {
            if (resetPreviousEvidence) {
                repository.clearCaseSessionEvidence(caseDef.id)
            }
            visitedZoneSet.clear()
            val (spawnX, spawnY) = FirstPersonEngine.findSpawnPosition(caseDef.mapGrid)
            val profile = profileState.value
            val difficulty = Difficulty.entries.find { it.name == profile.difficulty } ?: Difficulty.NORMAL
            val baseBattery = 100f + (profile.batteryUpgradeLevel - 1) * 20f
            val timeLimit = when {
                mode == GameMode.FREE -> null
                modifier == ChallengeModifier.BLITZ_TIMER -> 180
                else -> difficulty.timeLimitSeconds
            }

            val runtimeEntities = caseDef.entities.map {
                RuntimeEntity(def = it, x = it.startX, y = it.startY)
            }

            _activeInvestigation.value = ActiveInvestigationState(
                caseDef = caseDef,
                gameMode = mode,
                challengeModifier = modifier,
                player = Player3DState(x = spawnX, y = spawnY, yaw = 0f),
                entities = runtimeEntities,
                batteryPercent = baseBattery.coerceAtMost(160f),
                timeRemainingSeconds = timeLimit,
                hudBannerMessage = "${caseDef.caseNumber}: ${caseDef.title} — Investigation Active"
            )
            startInvestigationLoop()
        }
    }

    fun setPause(paused: Boolean) {
        _activeInvestigation.update { it?.copy(isPaused = paused) }
    }

    fun exitInvestigation() {
        stopInvestigationLoop()
        _activeInvestigation.value = null
    }

    fun setMoveInput(strafe: Float, forward: Float) {
        moveInputX = strafe.coerceIn(-1f, 1f)
        moveInputY = forward.coerceIn(-1f, 1f)
    }

    fun adjustLook(deltaYaw: Float, deltaPitch: Float) {
        val sens = profileState.value.lookSensitivity
        _activeInvestigation.update { state ->
            state ?: return@update null
            if (state.isPaused || state.inspectedObject != null) return@update state
            state.copy(
                player = state.player.copy(
                    yaw = state.player.yaw + deltaYaw * 0.0045f * sens,
                    pitchOffset = (state.player.pitchOffset + deltaPitch * 0.0018f * sens).coerceIn(-0.35f, 0.35f)
                )
            )
        }
    }

    fun toggleCrouch() {
        _activeInvestigation.update { state ->
            state ?: return@update null
            state.copy(player = state.player.copy(isCrouching = !state.player.isCrouching, isRunning = false))
        }
    }

    fun toggleRun() {
        _activeInvestigation.update { state ->
            state ?: return@update null
            state.copy(player = state.player.copy(isRunning = !state.player.isRunning, isCrouching = false))
        }
    }

    fun selectEquipment(equipment: EquipmentType?) {
        val current = _activeInvestigation.value ?: return
        if (equipment != null) {
            if (current.challengeModifier == ChallengeModifier.NO_THERMAL && equipment == EquipmentType.THERMAL_VISION) {
                showHudBanner("Thermal Vision disabled under Challenge Protocol.")
                return
            }
            if (current.challengeModifier == ChallengeModifier.NO_CAMERA && equipment == EquipmentType.EVIDENCE_CAMERA) {
                showHudBanner("Evidence Camera disabled under Challenge Protocol.")
                return
            }
            if (current.batteryPercent <= 0f && current.gameMode != GameMode.FREE) {
                showHudBanner("Equipment battery depleted. Rely on visual observation.")
                return
            }
        }

        val next = if (current.activeEquipment == equipment) null else equipment
        if (next != null) {
            audioEngine.playEquipmentPulse(next)
            if (next == EquipmentType.REALITY_SCANNER) {
                _activeInvestigation.update {
                    it?.copy(
                        activeEquipment = next,
                        scannerPulseProgress = 0.01f,
                        adaptation = it.adaptation.copy(scannerPulses = it.adaptation.scannerPulses + 1)
                    )
                }
                return
            }
            if (next == EquipmentType.EVIDENCE_CAMERA) {
                captureEvidencePhoto()
                return
            }
        }
        _activeInvestigation.update { it?.copy(activeEquipment = next) }
    }

    fun captureEvidencePhoto() {
        val state = _activeInvestigation.value ?: return
        if (state.challengeModifier == ChallengeModifier.NO_CAMERA) {
            showHudBanner("Evidence Camera disabled in this challenge.")
            return
        }
        audioEngine.playEquipmentPulse(EquipmentType.EVIDENCE_CAMERA)
        val target = state.lookTarget
        val newAdaptCount = state.adaptation.cameraUsageCount + 1

        _activeInvestigation.update {
            it?.copy(
                cameraFlashAlpha = 0.85f,
                adaptation = it.adaptation.copy(cameraUsageCount = newAdaptCount)
            )
        }

        if (target != null && target.linkedEvidenceId != null) {
            val evId = resolveEvidenceIdForObject(target, state.adaptation)
            viewModelScope.launch {
                repository.recordEvidence(state.caseDef.id, evId, photographed = true)
            }
            showHudBanner("Photo logged: ${target.name}")
        } else {
            showHudBanner("Snapshot captured. Aim reticle at anomalous object for forensic tagging.")
        }
        evaluateAdaptationThresholds()
    }

    fun interactWithTarget() {
        val state = _activeInvestigation.value ?: return
        val target = state.lookTarget ?: return

        if (target.isDoor) {
            val coord = target.doorGridX to target.doorGridY
            val isOpen = coord in state.openDoors
            val nextDoors = if (isOpen) state.openDoors - coord else state.openDoors + coord
            audioEngine.playDoorToggle()

            if (target.isSecretRoomDoor && !isOpen && target.linkedEvidenceId != null) {
                viewModelScope.launch {
                    repository.recordEvidence(state.caseDef.id, target.linkedEvidenceId)
                }
                audioEngine.playEvidenceCollected()
                showHudBanner("Unlisted space breached! Evidence logged.")
            } else {
                showHudBanner(if (isOpen) "Closed ${target.name}" else "Opened ${target.name}")
            }
            _activeInvestigation.update { it?.copy(openDoors = nextDoors) }
            return
        }

        if (target.requiresCrouch && !state.player.isCrouching) {
            showHudBanner("Crouch (CROUCH button) to inspect low-level clue: ${target.name}")
            return
        }

        // Track mirror inspections for Intelligent Anomaly adaptation
        if (target.kind == ObjectKind.MIRROR) {
            _activeInvestigation.update {
                it?.copy(adaptation = it.adaptation.copy(mirrorInspections = it.adaptation.mirrorInspections + 1))
            }
            evaluateAdaptationThresholds()
        }

        // Open detailed close-up Object Inspection modal!
        _activeInvestigation.update { it?.copy(inspectedObject = target) }
    }

    fun collectEvidenceFromInspection(obj: InteractiveObject, usedEquipment: EquipmentType?) {
        val state = _activeInvestigation.value ?: return
        val evId = resolveEvidenceIdForObject(obj, state.adaptation)
        viewModelScope.launch {
            repository.recordEvidence(
                caseId = state.caseDef.id,
                evidenceId = evId,
                photographed = usedEquipment == EquipmentType.EVIDENCE_CAMERA,
                spectralAnalyzed = usedEquipment == EquipmentType.SPECTRAL_LENS
            )
            if (obj.requiresCrouch) {
                repository.unlockAchievement("ach_crouch_clue")
            }
            audioEngine.playEvidenceCollected()
            val evTitle = state.caseDef.evidencePool.find { it.id == evId }?.title ?: obj.name
            showHudBanner("Evidence Recorded: $evTitle")
        }
    }

    fun closeInspectionModal() {
        _activeInvestigation.update { it?.copy(inspectedObject = null) }
    }

    fun linkEvidenceOnBoard(caseId: String, evA: String, evB: String) {
        if (evA == evB) return
        viewModelScope.launch {
            repository.addBoardLink(caseId, evA, evB)
            val caseDef = CaseCatalogPart2.getCaseById(caseId)
            val currentLinks = repository.observeBoardLinks(caseId).first()
            val linkedIds = currentLinks.flatMap { listOf(it.evidenceA, it.evidenceB) }.toSet()
            if (caseDef?.validTheories?.any { it.isValidDeduction && it.requiredEvidenceIds.all { req -> req in linkedIds } } == true) {
                repository.unlockAchievement("ach_board_master")
            }
        }
    }

    fun removeBoardLink(linkId: String) {
        viewModelScope.launch {
            repository.removeBoardLink(linkId)
        }
    }

    fun submitCaseReport(submission: CaseReportSubmission, onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _activeInvestigation.value
            val caseDef = state?.caseDef ?: CaseCatalogPart2.getCaseById(submission.caseId) ?: return@launch
            val collectedRows = repository.observeCaseEvidence(caseDef.id).first()
            val collectedIds = collectedRows.map { it.evidenceId }.toSet()

            val classificationCorrect = submission.classification == caseDef.correctClassification
            val threatCorrect = submission.threatLevel == caseDef.correctThreatLevel
            val actionCorrect = submission.recommendedAction == caseDef.correctAction

            val coreEvidence = caseDef.evidencePool.filter { !it.isMisleadingRedHerring && !it.isConspiracySecret }
            val secretEvidence = caseDef.evidencePool.filter { it.isConspiracySecret }
            val foundCoreCount = coreEvidence.count { it.id in collectedIds }
            val secretDiscovered = secretEvidence.any { it.id in collectedIds }
            val redHerringsAttached = submission.selectedEvidenceIds.count { id ->
                caseDef.evidencePool.find { it.id == id }?.isMisleadingRedHerring == true
            }

            var accuracy = 0
            if (classificationCorrect) accuracy += 45
            if (threatCorrect) accuracy += 15
            if (actionCorrect) accuracy += 10
            val evRatio = if (coreEvidence.isNotEmpty()) (foundCoreCount.toFloat() / coreEvidence.size) else 1f
            accuracy += (evRatio * 25f).toInt()
            if (secretDiscovered) accuracy += 10
            accuracy -= (redHerringsAttached * 10)
            accuracy = accuracy.coerceIn(0, 100)

            val rating = when {
                accuracy >= 90 -> "S"
                accuracy >= 75 -> "A"
                accuracy >= 58 -> "B"
                accuracy >= 40 -> "C"
                else -> "F"
            }

            val timeTaken = state?.elapsedSeconds ?: 120
            val witnessSafety = if (classificationCorrect) 100 else 60
            val baseXp = when (rating) {
                "S" -> 280
                "A" -> 210
                "B" -> 150
                "C" -> 95
                else -> 40
            }
            val modifierBonus = state?.challengeModifier?.bonusCredits ?: 0
            val creditsEarned = (accuracy * 2) + (if (secretDiscovered) 60 else 0) + modifierBonus

            repository.saveCaseCompletion(
                caseId = caseDef.id,
                rating = rating,
                accuracy = accuracy,
                timeSeconds = timeTaken,
                secretDiscovered = secretDiscovered,
                classification = submission.classification.label
            )

            // Unlock next story case if passed
            var newlyUnlockedTitle: String? = null
            if (caseDef.isStoryCase && rating != "F") {
                val idx = CaseCatalogPart2.allStoryCases.indexOfFirst { it.id == caseDef.id }
                if (idx != -1 && idx + 1 < CaseCatalogPart2.allStoryCases.size) {
                    val nextCase = CaseCatalogPart2.allStoryCases[idx + 1]
                    repository.unlockCase(nextCase.id)
                    newlyUnlockedTitle = "${nextCase.caseNumber}: ${nextCase.title}"
                }
            }

            // Update profile & conspiracy tracking
            val profile = profileState.value
            val prevTwistSeen = profile.postCase001TwistSeen
            val triggerTwist = caseDef.id == "case_001" && !prevTwistSeen

            val conspiracyDelta = if (secretDiscovered || submission.recommendedAction == RecommendedAction.INTERNAL_AUDIT || submission.recommendedAction == RecommendedAction.DECLASSIFY) 1 else 0
            val loyaltyDelta = if (submission.recommendedAction == RecommendedAction.CONTAIN || submission.recommendedAction == RecommendedAction.SEAL_SITE) 1 else 0
            val failDelta = if (rating == "F") 1 else 0

            val newConspiracy = profile.conspiracyScore + conspiracyDelta
            val newLoyalty = profile.loyaltyScore + loyaltyDelta
            val newFailures = profile.failureCount + failDelta

            // Determine Ending if Case 008 completed or too many failures
            var unlockedEnding: EndingType? = null
            if (caseDef.id == "case_008") {
                val allProgress = caseProgressState.value.values
                val totalSecrets = allProgress.count { it.secretDiscovered } + (if (secretDiscovered) 1 else 0)
                unlockedEnding = when {
                    totalSecrets >= 6 && newConspiracy >= 4 -> EndingType.SECRET_FIFTH_RECORD
                    submission.recommendedAction == RecommendedAction.INTERNAL_AUDIT ||
                        submission.recommendedAction == RecommendedAction.DECLASSIFY -> EndingType.THE_TRUTH
                    newLoyalty > newConspiracy -> EndingType.THE_SUCCESSOR
                    secretDiscovered -> EndingType.THE_ERASED_INVESTIGATOR
                    else -> EndingType.THE_FINAL_FILE
                }
            } else if (newFailures >= 3) {
                unlockedEnding = EndingType.THE_CONTAINMENT_FAILURE
            }

            val updatedEndings = profile.unlockedEndingsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (unlockedEnding != null) updatedEndings.add(unlockedEnding.name)

            repository.updateProfile {
                it.copy(
                    xp = it.xp + baseXp,
                    credits = it.credits + creditsEarned,
                    conspiracyScore = newConspiracy,
                    loyaltyScore = newLoyalty,
                    failureCount = newFailures,
                    unlockedEndingsCsv = updatedEndings.joinToString(",")
                )
            }

            // Achievements
            if (caseDef.id == "case_001") repository.unlockAchievement("ach_first_case")
            if (rating == "S") repository.unlockAchievement("ach_perfect_s")
            if (state?.adaptation?.let { it.thermalMasked || it.reflectionsCorrupted || it.footageTampered } == true && classificationCorrect) {
                repository.unlockAchievement("ach_outsmart_entity")
            }
            if (newConspiracy >= 3) repository.unlockAchievement("ach_conspiracy_3")
            if (state?.gameMode == GameMode.ENDLESS && rating != "F") repository.unlockAchievement("ach_endless_run")
            if (state?.gameMode == GameMode.CHALLENGE && rating != "F") repository.unlockAchievement("ach_challenge_win")
            if (unlockedEnding == EndingType.SECRET_FIFTH_RECORD) repository.unlockAchievement("ach_secret_ending")

            val feedback = buildString {
                append(if (classificationCorrect) "Anomaly classification verified. " else "Misclassified phenomenon (Expected: ${caseDef.correctClassification.label}). ")
                if (redHerringsAttached > 0) append("$redHerringsAttached misleading clue(s) reduced forensic score. ")
                if (secretDiscovered) append("CLASSIFIED FRAGMENT LOGGED. ")
            }

            _lastEvaluation.value = CaseEvaluationResult(
                caseId = caseDef.id,
                rating = rating,
                accuracyPercent = accuracy,
                evidenceFoundCount = foundCoreCount,
                totalCoreEvidenceCount = coreEvidence.size,
                secretDiscovered = secretDiscovered,
                timeTakenSeconds = timeTaken,
                witnessSafetyScore = witnessSafety,
                xpEarned = baseXp,
                creditsEarned = creditsEarned,
                classificationCorrect = classificationCorrect,
                feedbackSummary = feedback,
                newlyUnlockedCaseTitle = newlyUnlockedTitle,
                triggerPostCase001Twist = triggerTwist,
                unlockedEnding = unlockedEnding
            )

            stopInvestigationLoop()
            _activeInvestigation.value = null
            onComplete()
        }
    }

    private fun resolveEvidenceIdForObject(obj: InteractiveObject, adaptation: AdaptationState): String {
        if (obj.alternateEvidenceIfAdapted != null &&
            (adaptation.footageTampered || adaptation.decoyClueSpawned)
        ) {
            return obj.alternateEvidenceIfAdapted
        }
        return obj.linkedEvidenceId ?: ""
    }

    private fun showHudBanner(msg: String) {
        _activeInvestigation.update { it?.copy(hudBannerMessage = msg) }
    }

    private fun evaluateAdaptationThresholds() {
        val state = _activeInvestigation.value ?: return
        val diff = Difficulty.entries.find { it.name == profileState.value.difficulty } ?: Difficulty.NORMAL
        val challengeMult = if (state.challengeModifier == ChallengeModifier.HYPER_ADAPTIVE) 0.5f else 1f
        val mult = diff.adaptationThresholdMultiplier * challengeMult

        val thermalLimit = (320 * mult).toInt().coerceAtLeast(120) // ~10s active thermal
        val mirrorLimit = (3 * mult).toInt().coerceAtLeast(2)
        val cameraLimit = (4 * mult).toInt().coerceAtLeast(2)

        var adapt = state.adaptation
        var alertMsg: String? = null

        if (!adapt.thermalMasked && adapt.thermalUsageTicks >= thermalLimit) {
            adapt = adapt.copy(
                thermalMasked = true,
                adaptationWarnings = adapt.adaptationWarnings + "Thermal masking detected"
            )
            alertMsg = "ADAPTATION ALERT: Entity dropped thermal signature to ambient!"
        }
        if (!adapt.reflectionsCorrupted && adapt.mirrorInspections >= mirrorLimit) {
            adapt = adapt.copy(
                reflectionsCorrupted = true,
                adaptationWarnings = adapt.adaptationWarnings + "Reflective manipulation active"
            )
            alertMsg = "ADAPTATION ALERT: Mirror reflections are no longer synchronized!"
        }
        if (!adapt.footageTampered && adapt.cameraUsageCount >= cameraLimit) {
            adapt = adapt.copy(
                footageTampered = true,
                adaptationWarnings = adapt.adaptationWarnings + "Surveillance logs overwritten"
            )
            alertMsg = "ADAPTATION ALERT: Security logs are being rewritten!"
        }

        if (alertMsg != null) {
            audioEngine.playAnomalyAlert()
            _activeInvestigation.update { it?.copy(adaptation = adapt, hudBannerMessage = alertMsg) }
        }
    }

    private fun startInvestigationLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            val dt = 0.033f // ~30fps state tick for smooth Compose rendering & low CPU
            while (true) {
                delay(33L)
                val state = _activeInvestigation.value ?: break
                if (state.isPaused || state.inspectedObject != null) continue

                val diff = Difficulty.entries.find { it.name == profileState.value.difficulty } ?: Difficulty.NORMAL
                var player = state.player

                // 1. Movement & Collision
                val speed = when {
                    player.isCrouching -> 1.55f
                    player.isRunning -> 3.65f
                    else -> 2.4f
                }
                val fwdX = cos(player.yaw)
                val fwdY = sin(player.yaw)
                val rightX = -fwdY
                val rightY = fwdX

                val vx = (fwdX * moveInputY + rightX * moveInputX) * speed * dt
                val vy = (fwdY * moveInputY + rightY * moveInputX) * speed * dt

                var nextX = player.x
                var nextY = player.y
                var moved = false
                if (hypot(vx, vy) > 0.001f) {
                    if (FirstPersonEngine.isWalkable(nextX + vx, nextY, state.caseDef.mapGrid, state.openDoors)) {
                        nextX += vx
                        moved = true
                    }
                    if (FirstPersonEngine.isWalkable(nextX, nextY + vy, state.caseDef.mapGrid, state.openDoors)) {
                        nextY += vy
                        moved = true
                    }
                }

                val nextBob = if (moved) player.bobPhase + dt * (if (player.isRunning) 14f else 9f) else player.bobPhase
                player = player.copy(x = nextX, y = nextY, bobPhase = nextBob)

                if (moved) {
                    stepAudioCooldown -= dt
                    if (stepAudioCooldown <= 0f) {
                        audioEngine.playFootstep(player.isRunning)
                        stepAudioCooldown = if (player.isRunning) 0.32f else 0.48f
                    }
                    val cell = nextX.toInt() to nextY.toInt()
                    val count = (visitedZoneSet[cell] ?: 0) + 1
                    visitedZoneSet[cell] = count
                }

                // 2. Entity AI Patrol & Observer Vanishing
                val updatedEntities = state.entities.map { ent ->
                    val distToPlayer = hypot(ent.x - nextX, ent.y - nextY)
                    if (ent.def.disappearsOnApproach && distToPlayer < 2.2f && !ent.vanished) {
                        audioEngine.playAnomalyAlert()
                        ent.copy(vanished = true)
                    } else if (!ent.vanished && ent.def.patrolPoints.isNotEmpty()) {
                        val targetPt = ent.def.patrolPoints[ent.patrolIndex % ent.def.patrolPoints.size]
                        val dx = targetPt.first - ent.x
                        val dy = targetPt.second - ent.y
                        val d = hypot(dx, dy)
                        if (d < 0.15f) {
                            ent.copy(patrolIndex = (ent.patrolIndex + 1) % ent.def.patrolPoints.size)
                        } else {
                            val entSpeed = 0.65f * dt
                            ent.copy(
                                x = ent.x + (dx / d) * entSpeed,
                                y = ent.y + (dy / d) * entSpeed
                            )
                        }
                    } else ent
                }

                // 3. Equipment Battery & Pulse Animation
                var battery = state.batteryPercent
                var activeEquip = state.activeEquipment
                var adapt = state.adaptation
                if (activeEquip != null && state.gameMode != GameMode.FREE) {
                    battery = (battery - activeEquip.drainPerSecond * diff.batteryDrainMultiplier * dt).coerceAtLeast(0f)
                    if (battery <= 0f) {
                        activeEquip = null
                    }
                    if (activeEquip == EquipmentType.THERMAL_VISION) {
                        adapt = adapt.copy(thermalUsageTicks = adapt.thermalUsageTicks + 1)
                    }
                } else if (activeEquip == null) {
                    battery = (battery + 0.6f * dt).coerceAtMost(100f)
                }

                val nextPulse = if (state.scannerPulseProgress > 0f) {
                    val p = state.scannerPulseProgress + dt * 1.15f
                    if (p > 1f) 0f else p
                } else 0f

                val emf = FirstPersonEngine.calculateEmfNearLook(player, state.caseDef.objects, updatedEntities)
                if (activeEquip == EquipmentType.SIGNAL_DETECTOR && emf > 0.25f) {
                    emfTickCooldown -= dt
                    if (emfTickCooldown <= 0f) {
                        audioEngine.playEmfTick(emf)
                        emfTickCooldown = (0.55f - emf * 0.4f).coerceAtLeast(0.12f)
                    }
                }

                val target = FirstPersonEngine.findLookTarget(player, state.caseDef.objects, state.openDoors)
                val nextFlash = (state.cameraFlashAlpha - dt * 2.5f).coerceAtLeast(0f)

                secondAccumulator += dt
                var elapsed = state.elapsedSeconds
                var remaining = state.timeRemainingSeconds
                if (secondAccumulator >= 1f) {
                    secondAccumulator -= 1f
                    elapsed += 1
                    if (remaining != null && remaining > 0) remaining -= 1
                }

                _activeInvestigation.value = state.copy(
                    player = player,
                    entities = updatedEntities,
                    activeEquipment = activeEquip,
                    batteryPercent = battery,
                    scannerPulseProgress = nextPulse,
                    emfStrength = emf,
                    lookTarget = target,
                    adaptation = adapt,
                    cameraFlashAlpha = nextFlash,
                    elapsedSeconds = elapsed,
                    timeRemainingSeconds = remaining,
                    animTimeSec = state.animTimeSec + dt
                )

                if (adapt.thermalUsageTicks % 30 == 1) {
                    evaluateAdaptationThresholds()
                }
            }
        }
    }

    private fun stopInvestigationLoop() {
        loopJob?.cancel()
        loopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopInvestigationLoop()
    }
}
