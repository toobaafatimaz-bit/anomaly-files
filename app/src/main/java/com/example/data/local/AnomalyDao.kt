package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnomalyDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM case_progress")
    fun observeAllCaseProgress(): Flow<List<CaseProgressEntity>>

    @Query("SELECT * FROM case_progress WHERE caseId = :caseId")
    suspend fun getCaseProgress(caseId: String): CaseProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCaseProgress(progress: CaseProgressEntity)

    @Query("SELECT * FROM collected_evidence WHERE caseId = :caseId")
    fun observeEvidenceForCase(caseId: String): Flow<List<CollectedEvidenceEntity>>

    @Query("SELECT * FROM collected_evidence")
    fun observeAllEvidence(): Flow<List<CollectedEvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvidence(evidence: CollectedEvidenceEntity)

    @Query("DELETE FROM collected_evidence WHERE caseId = :caseId")
    suspend fun clearEvidenceForCase(caseId: String)

    @Query("SELECT * FROM board_links WHERE caseId = :caseId")
    fun observeBoardLinks(caseId: String): Flow<List<BoardLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoardLink(link: BoardLinkEntity)

    @Query("DELETE FROM board_links WHERE id = :linkId")
    suspend fun deleteBoardLink(linkId: String)

    @Query("DELETE FROM board_links WHERE caseId = :caseId")
    suspend fun clearBoardLinksForCase(caseId: String)

    @Query("SELECT * FROM achievements")
    fun observeAchievements(): Flow<List<UnlockedAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: UnlockedAchievementEntity)

    @Query("DELETE FROM case_progress")
    suspend fun clearAllCaseProgress()

    @Query("DELETE FROM collected_evidence")
    suspend fun clearAllEvidence()

    @Query("DELETE FROM board_links")
    suspend fun clearAllBoardLinks()
}
