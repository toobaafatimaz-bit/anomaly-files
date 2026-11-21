package com.example.model

import androidx.compose.ui.graphics.Color

enum class DetectiveRank(
    val title: String,
    val level: Int,
    val minXp: Int,
    val clearanceCode: String
) {
    TRAINEE("Trainee", 1, 0, "LVL-1"),
    JUNIOR_INVESTIGATOR("Junior Investigator", 2, 250, "LVL-2"),
    FIELD_DETECTIVE("Field Detective", 3, 600, "LVL-3"),
    SENIOR_DETECTIVE("Senior Detective", 4, 1100, "LVL-4"),
    SPECIAL_INVESTIGATOR("Special Investigator", 5, 1750, "LVL-5"),
    CHIEF_INVESTIGATOR("Chief Investigator", 6, 2600, "LVL-6"),
    DIRECTOR("Director", 7, 3800, "LVL-7");

    companion object {
        fun fromXp(xp: Int): DetectiveRank =
            entries.lastOrNull { xp >= it.minXp } ?: TRAINEE
    }
}

enum class Difficulty(
    val label: String,
    val batteryDrainMultiplier: Float,
    val adaptationThresholdMultiplier: Float,
    val timeLimitSeconds: Int,
    val showHints: Boolean,
    val description: String
) {
    EASY(
        "EASY",
        0.65f,
        1.5f,
        900,
        true,
        "Extended battery, subtle anomaly hints, slower entity adaptation."
    ),
    NORMAL(
        "NORMAL",
        1.0f,
        1.0f,
        600,
        false,
        "Standard DUI field protocol. Balanced battery and intelligent adaptation."
    ),
    HARD(
        "HARD",
        1.45f,
        0.65f,
        420,
        false,
        "Strict battery drain, rapid anomaly counter-adaptation, extra misleading clues."
    )
}

enum class GameMode(val label: String) {
    STORY("Story Mode"),
    ENDLESS("Endless Investigation"),
    CHALLENGE("Challenge Mode"),
    FREE("Free Investigation")
}

enum class ChallengeModifier(
    val title: String,
    val description: String,
    val bonusCredits: Int
) {
    NO_THERMAL("Cold Blindness", "Thermal Vision hardware disabled.", 150),
    BLITZ_TIMER("Containment Window", "Complete investigation in 180 seconds.", 200),
    NO_CAMERA("Analog Protocol", "Evidence Camera disabled. Rely on physical/audio logs.", 175),
    HYPER_ADAPTIVE("Mimic Surge", "Intelligent anomalies adapt twice as fast.", 250)
}

enum class EquipmentType(
    val label: String,
    val shortCode: String,
    val description: String,
    val drainPerSecond: Float,
    val unlockCost: Int,
    val isDefaultUnlocked: Boolean
) {
    REALITY_SCANNER(
        "Reality Scanner",
        "SCN",
        "Measures spatial/temporal coherence waves and highlights anomalous geometry.",
        2.2f,
        0,
        true
    ),
    THERMAL_VISION(
        "Thermal Vision",
        "THM",
        "FLIR heat-signature filter. Reveals hidden warm entities and cold spots.",
        3.0f,
        0,
        true
    ),
    AUDIO_ANALYZER(
        "Audio Analyzer",
        "AUD",
        "Captures sub-audible EVP whispers and distorted acoustic frequencies.",
        1.8f,
        0,
        true
    ),
    MEMORY_RECORDER(
        "Memory Recorder",
        "MEM",
        "Reconstructs ghostly temporal echoes of past occupants in 3D space.",
        4.2f,
        120,
        false
    ),
    SPECTRAL_LENS(
        "Spectral Lens",
        "SPC",
        "Ultraviolet optical filter exposing invisible markings and tampered photos.",
        2.6f,
        0,
        true
    ),
    EVIDENCE_CAMERA(
        "Evidence Camera",
        "CAM",
        "Captures forensic snapshots of objects or entities for the evidence log.",
        0.5f,
        0,
        true
    ),
    SIGNAL_DETECTOR(
        "Signal Detector",
        "EMF",
        "Directional electromagnetic tracker for tampered electronics and entities.",
        1.5f,
        80,
        false
    )
}

enum class EvidenceCategory(val label: String) {
    PHOTOGRAPHIC("Photographic"),
    AUDIO("Audio"),
    PHYSICAL("Physical"),
    WITNESS("Witness Testimony"),
    DIGITAL("Digital"),
    ENVIRONMENTAL("Environmental")
}

enum class AnomalyCategory(val label: String) {
    VISUAL("Visual Anomaly"),
    BIOLOGICAL("Biological Anomaly"),
    SPATIAL("Spatial Anomaly"),
    TEMPORAL("Temporal Anomaly"),
    INTELLIGENT("Intelligent Anomaly"),
    LOGICAL_EXPLANATION("Logical / Human Factor")
}

enum class AnomalyClassification(val label: String, val threatSummary: String) {
    NORMAL_OCCURRENCE("Normal Occurrence", "Mundane environmental cause; no supernatural factor."),
    HUMAN_DECEPTION("Human Deception", "Staged evidence or human intrusion without anomalous traits."),
    UNKNOWN_ANOMALY("Unknown Anomaly", "Verified unexplained phenomenon requiring containment study."),
    DANGEROUS_ANOMALY("Dangerous Anomaly", "Hostile or reality-destabilizing entity present."),
    INTELLIGENT_ANOMALY("Intelligent Anomaly", "Self-aware phenomenon actively adapting to investigators."),
    UNCLASSIFIED_PHENOMENON("Unclassified Phenomenon", "Exceeds standard DUI taxonomy; linked to internal conspiracy.")
}

enum class ThreatLevel(val label: String) {
    CLASS_I("Class I — Negligible"),
    CLASS_II("Class II — Moderate"),
    CLASS_III("Class III — Severe"),
    CLASS_IV("Class IV — Critical"),
    CLASS_V("Class V — Omega / Redacted")
}

enum class RecommendedAction(val label: String) {
    OBSERVE("Passive Observation"),
    CONTAIN("Standard Containment Seal"),
    DECLASSIFY("Public Incident Report"),
    SEAL_SITE("Permanent Architectural Quarantine"),
    INTERNAL_AUDIT("Trigger DUI Internal Audit")
}

enum class EntityType(
    val entityName: String,
    val behaviorSummary: String,
    val detectionMethod: String,
    val weakness: String
) {
    THE_OBSERVER(
        "THE OBSERVER",
        "Watches silently from distant corridors; vanishes when approached directly.",
        "Evidence Camera from distance or Reality Scanner wave.",
        "Does not pursue; reveals hidden spatial flaws when tracked."
    ),
    THE_COPY(
        "THE COPY",
        "Imitates human posture and footsteps, but reflections and thermal timing lag behind.",
        "Spectral Lens or Mirror inspection.",
        "Makes micro-mistakes in mirrored symmetry."
    ),
    THE_SILENT(
        "THE SILENT",
        "Zero normal sound profile; only detectable via sub-audible EVP spikes or EMF interference.",
        "Audio Analyzer EVP mode or Signal Detector.",
        "Freezes when caught inside an active Reality Scanner pulse."
    ),
    THE_ARCHIVIST(
        "THE ARCHIVIST",
        "Alters dates, photographs, and security records after the investigator inspects them.",
        "Compare Memory Recorder echoes against Digital/Photographic logs.",
        "Cannot erase physical scratches beneath floor level."
    ),
    THE_REPLACEMENT(
        "THE REPLACEMENT",
        "Gradually swaps rooms and people with nearly identical duplicates when unobserved.",
        "Thermal Vision + Memory Recorder comparison.",
        "Leaves cold geometric seams along modified door frames."
    )
}

enum class EndingType(
    val title: String,
    val subtitle: String,
    val narrative: String
) {
    THE_TRUTH(
        "ENDING I: THE TRUTH",
        "You leaked the Fifth Division archives.",
        "By refusing DUI containment orders and assembling the classified fragments from Blackwood, St. Jude, and Channel 6, you proved the Department wasn't containing anomalies—it was harvesting human memories to anchor unstable realities. The broadcast went live at 03:17."
    ),
    THE_SUCCESSOR(
        "ENDING II: THE SUCCESSOR",
        "You accepted the Director's chair.",
        "Order requires silence. You sealed the final breach at Channel 6, classified every anomalous file, and signed your own clearance renewal. Outside your office window, the city sleeps—and in your personnel photo, the figure behind you now wears your coat."
    ),
    THE_ERASED_INVESTIGATOR(
        "ENDING III: THE ERASED INVESTIGATOR",
        "Your personnel file predates your birth.",
        "Inside Sub-Level Lab 07 you uncovered Project Mirror-0. There was no previous assignment you forgot—you were synthesized from the survivor of Apartment 4B. Every case was a calibration test."
    ),
    THE_CONTAINMENT_FAILURE(
        "ENDING IV: CONTAINMENT FAILURE",
        "Misclassification broke the perimeter.",
        "Too many false conclusions allowed the Intelligent Anomalies to map DUI protocols. The hallway behind Sector 04 no longer ends at the elevator—it loops endlessly back to Blackwood Apartment 4B."
    ),
    THE_FINAL_FILE(
        "ENDING V: THE FINAL FILE",
        "Case #000 was always about you.",
        "The anomalies recognized you because you have walked these corridors hundreds of times before. Every time you get close to the truth, the Department resets your memory—and starts the briefing tape again."
    ),
    SECRET_FIFTH_RECORD(
        "SECRET ENDING: THE FIFTH RESIDENT",
        "All hidden conspiracy fragments decoded.",
        "Across all eight locations, you uncovered every redacted cipher and never fell for a single decoy. You didn't just understand the fifth figure in your photograph—you stepped out of the frame with them, leaving DUI Sector 04 permanently empty."
    )
}

enum class ObjectKind {
    PHOTOGRAPH,
    DOCUMENT,
    CLOCK,
    MIRROR,
    TV_SCREEN,
    SECURITY_CAMERA,
    COMPUTER,
    DOOR,
    WINDOW,
    BELONGING,
    AUDIO_TAPE,
    WALL_MARKING,
    FLOOR_CLUE
}

enum class WallTheme(
    val primaryColor: Color,
    val trimColor: Color,
    val floorColor: Color,
    val ceilingColor: Color,
    val ambientFogColor: Color
) {
    APARTMENT(
        Color(0xFF2B323D),
        Color(0xFF1A2029),
        Color(0xFF151921),
        Color(0xFF0B0E14),
        Color(0xFF0A1018)
    ),
    HOTEL(
        Color(0xFF3A2229),
        Color(0xFF221318),
        Color(0xFF1D1116),
        Color(0xFF0E080B),
        Color(0xFF160B10)
    ),
    SCHOOL(
        Color(0xFF2C3530),
        Color(0xFF1B231E),
        Color(0xFF191D1A),
        Color(0xFF0D110E),
        Color(0xFF0C1410)
    ),
    SUBWAY(
        Color(0xFF273138),
        Color(0xFF171E24),
        Color(0xFF12161A),
        Color(0xFF080B0E),
        Color(0xFF091016)
    ),
    HOSPITAL(
        Color(0xFF323C45),
        Color(0xFF1D252C),
        Color(0xFF1B2228),
        Color(0xFF0E141A),
        Color(0xFF0D161E)
    ),
    LABORATORY(
        Color(0xFF222D3D),
        Color(0xFF121924),
        Color(0xFF101620),
        Color(0xFF060A10),
        Color(0xFF08121E)
    ),
    MIRROR_HOUSE(
        Color(0xFF2E273B),
        Color(0xFF1B1524),
        Color(0xFF16111D),
        Color(0xFF0A070F),
        Color(0xFF130B20)
    ),
    BROADCAST(
        Color(0xFF2A262B),
        Color(0xFF19161A),
        Color(0xFF141215),
        Color(0xFF080709),
        Color(0xFF180A0C)
    )
}

data class EvidenceDefinition(
    val id: String,
    val title: String,
    val category: EvidenceCategory,
    val summary: String,
    val detailedAnalysis: String,
    val spectralNote: String? = null,
    val thermalNote: String? = null,
    val audioTranscript: String? = null,
    val memoryEchoText: String? = null,
    val anomalyCategory: AnomalyCategory,
    val isMisleadingRedHerring: Boolean = false,
    val isConspiracySecret: Boolean = false,
    val requiredEquipmentToUnlock: EquipmentType? = null
)

data class InteractiveObject(
    val id: String,
    val name: String,
    val kind: ObjectKind,
    val gridX: Float,
    val gridY: Float,
    val requiresCrouch: Boolean = false,
    val promptText: String,
    val baseDescription: String,
    val spectralDescription: String? = null,
    val thermalDescription: String? = null,
    val audioSnippet: String? = null,
    val memoryEchoDescription: String? = null,
    val emfStrength: Float = 0.1f,
    val realityDistortion: Float = 0.0f,
    val linkedEvidenceId: String? = null,
    val alternateEvidenceIfAdapted: String? = null,
    val isDoor: Boolean = false,
    val doorGridX: Int = -1,
    val doorGridY: Int = -1,
    val isSecretRoomDoor: Boolean = false,
    val witnessName: String? = null,
    val witnessStatement: String? = null
)

data class SceneEntity(
    val id: String,
    val type: EntityType,
    val startX: Float,
    val startY: Float,
    val patrolPoints: List<Pair<Float, Float>>,
    val disappearsOnApproach: Boolean = false,
    val visibleOnlyInSpectralOrMirror: Boolean = false,
    val baseHeat: Float = 0.7f
)

data class TheoryHypothesis(
    val id: String,
    val requiredEvidenceIds: Set<String>,
    val title: String,
    val conclusion: String,
    val isValidDeduction: Boolean,
    val unlocksSecretClue: Boolean = false
)

data class CaseDefinition(
    val id: String,
    val caseNumber: String,
    val title: String,
    val locationName: String,
    val incidentSummary: String,
    val witnessCount: Int,
    val suspiciousActivities: List<String>,
    val threatClassification: ThreatLevel,
    val primaryObjective: String,
    val optionalSecretObjective: String,
    val wallTheme: WallTheme,
    val mapGrid: List<String>, // '#' wall, '.' floor, 'D' door, 'M' mirror, 'S' secret door, 'P' player spawn
    val objects: List<InteractiveObject>,
    val evidencePool: List<EvidenceDefinition>,
    val entities: List<SceneEntity>,
    val validTheories: List<TheoryHypothesis>,
    val correctClassification: AnomalyClassification,
    val correctThreatLevel: ThreatLevel,
    val correctAction: RecommendedAction,
    val conspiracyRevealText: String,
    val isStoryCase: Boolean = true,
    val unlockRequirementCaseId: String? = null
)

data class AdaptationState(
    val thermalUsageTicks: Int = 0,
    val mirrorInspections: Int = 0,
    val cameraUsageCount: Int = 0,
    val scannerPulses: Int = 0,
    val repeatedStepsInSameZone: Int = 0,
    val thermalMasked: Boolean = false,
    val reflectionsCorrupted: Boolean = false,
    val footageTampered: Boolean = false,
    val decoyClueSpawned: Boolean = false,
    val adaptationWarnings: List<String> = emptyList()
)

data class CaseReportSubmission(
    val caseId: String,
    val classification: AnomalyClassification,
    val threatLevel: ThreatLevel,
    val recommendedAction: RecommendedAction,
    val selectedEvidenceIds: Set<String>,
    val theoryExplanation: String
)

data class CaseEvaluationResult(
    val caseId: String,
    val rating: String, // S, A, B, C, F
    val accuracyPercent: Int,
    val evidenceFoundCount: Int,
    val totalCoreEvidenceCount: Int,
    val secretDiscovered: Boolean,
    val timeTakenSeconds: Int,
    val witnessSafetyScore: Int,
    val xpEarned: Int,
    val creditsEarned: Int,
    val classificationCorrect: Boolean,
    val feedbackSummary: String,
    val newlyUnlockedCaseTitle: String? = null,
    val triggerPostCase001Twist: Boolean = false,
    val unlockedEnding: EndingType? = null
)

data class AchievementInfo(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false
)
