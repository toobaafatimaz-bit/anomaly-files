package com.example.data.repository

import com.example.data.local.AnomalyDao
import com.example.data.local.BoardLinkEntity
import com.example.data.local.CaseProgressEntity
import com.example.data.local.CollectedEvidenceEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.UnlockedAchievementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: AnomalyDao) {

    val profileFlow: Flow<PlayerProfileEntity> = dao.observeProfile().map {
        it ?: PlayerProfileEntity()
    }

    val caseProgressFlow: Flow<Map<String, CaseProgressEntity>> =
        dao.observeAllCaseProgress().map { list -> list.associateBy { it.caseId } }

    val allEvidenceFlow: Flow<List<CollectedEvidenceEntity>> = dao.observeAllEvidence()

    val unlockedAchievementsFlow: Flow<Set<String>> =
        dao.observeAchievements().map { list -> list.map { it.id }.toSet() }

    fun observeCaseEvidence(caseId: String): Flow<List<CollectedEvidenceEntity>> =
        dao.observeEvidenceForCase(caseId)

    fun observeBoardLinks(caseId: String): Flow<List<BoardLinkEntity>> =
        dao.observeBoardLinks(caseId)

    suspend fun ensureInitialized() {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(PlayerProfileEntity())
        }
        if (dao.getCaseProgress("case_001") == null) {
            dao.upsertCaseProgress(CaseProgressEntity(caseId = "case_001", unlocked = true))
        }
    }

    suspend fun resetForNewGame() {
        dao.clearAllCaseProgress()
        dao.clearAllEvidence()
        dao.clearAllBoardLinks()
        dao.upsertProfile(PlayerProfileEntity())
        dao.upsertCaseProgress(CaseProgressEntity(caseId = "case_001", unlocked = true))
    }

    suspend fun updateProfile(transform: (PlayerProfileEntity) -> PlayerProfileEntity) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(transform(current))
    }

    suspend fun unlockCase(caseId: String) {
        val existing = dao.getCaseProgress(caseId)
        if (existing == null) {
            dao.upsertCaseProgress(CaseProgressEntity(caseId = caseId, unlocked = true))
        } else if (!existing.unlocked) {
            dao.upsertCaseProgress(existing.copy(unlocked = true))
        }
    }

    suspend fun saveCaseCompletion(
        caseId: String,
        rating: String,
        accuracy: Int,
        timeSeconds: Int,
        secretDiscovered: Boolean,
        classification: String
    ) {
        val existing = dao.getCaseProgress(caseId)
        val betterRating = pickBetterRating(existing?.bestRating ?: "-", rating)
        val bestAcc = maxOf(existing?.bestAccuracy ?: 0, accuracy)
        val bestTime = if (existing == null || existing.bestTimeSeconds == 0) timeSeconds else minOf(existing.bestTimeSeconds, timeSeconds)
        dao.upsertCaseProgress(
            CaseProgressEntity(
                caseId = caseId,
                unlocked = true,
                completed = true,
                bestRating = betterRating,
                bestAccuracy = bestAcc,
                bestTimeSeconds = bestTime,
                secretDiscovered = (existing?.secretDiscovered == true) || secretDiscovered,
                lastClassification = classification
            )
        )
    }

    suspend fun recordEvidence(
        caseId: String,
        evidenceId: String,
        photographed: Boolean = false,
        spectralAnalyzed: Boolean = false
    ) {
        val composite = "$caseId::$evidenceId"
        dao.upsertEvidence(
            CollectedEvidenceEntity(
                compositeId = composite,
                caseId = caseId,
                evidenceId = evidenceId,
                photographed = photographed,
                spectralAnalyzed = spectralAnalyzed
            )
        )
    }

    suspend fun clearCaseSessionEvidence(caseId: String) {
        dao.clearEvidenceForCase(caseId)
        dao.clearBoardLinksForCase(caseId)
    }

    suspend fun addBoardLink(caseId: String, evA: String, evB: String) {
        val sorted = listOf(evA, evB).sorted()
        val id = "$caseId::${sorted[0]}::${sorted[1]}"
        dao.insertBoardLink(
            BoardLinkEntity(
                id = id,
                caseId = caseId,
                evidenceA = sorted[0],
                evidenceB = sorted[1]
            )
        )
    }

    suspend fun removeBoardLink(linkId: String) {
        dao.deleteBoardLink(linkId)
    }

    suspend fun unlockAchievement(id: String) {
        dao.unlockAchievement(UnlockedAchievementEntity(id = id))
    }

    private fun pickBetterRating(old: String, new: String): String {
        val order = listOf("S", "A", "B", "C", "F", "-")
        val oldIdx = order.indexOf(old).let { if (it == -1) 99 else it }
        val newIdx = order.indexOf(new).let { if (it == -1) 99 else it }
        return if (newIdx <= oldIdx) new else old
    }
}
