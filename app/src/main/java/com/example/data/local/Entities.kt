package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val investigatorCode: String = "AGENT-049",
    val xp: Int = 0,
    val credits: Int = 100,
    val difficulty: String = "NORMAL",
    val soundEnabled: Boolean = true,
    val lookSensitivity: Float = 1.0f,
    val activeOutfit: String = "Standard DUI Trench",
    val unlockedEquipmentCsv: String = "REALITY_SCANNER,THERMAL_VISION,AUDIO_ANALYZER,SPECTRAL_LENS,EVIDENCE_CAMERA",
    val unlockedEndingsCsv: String = "",
    val introCompleted: Boolean = false,
    val postCase001TwistSeen: Boolean = false,
    val conspiracyScore: Int = 0,
    val loyaltyScore: Int = 0,
    val failureCount: Int = 0,
    val batteryUpgradeLevel: Int = 1
)

@Entity(tableName = "case_progress")
data class CaseProgressEntity(
    @PrimaryKey val caseId: String,
    val unlocked: Boolean = false,
    val completed: Boolean = false,
    val bestRating: String = "-",
    val bestAccuracy: Int = 0,
    val bestTimeSeconds: Int = 0,
    val secretDiscovered: Boolean = false,
    val lastClassification: String = ""
)

@Entity(tableName = "collected_evidence")
data class CollectedEvidenceEntity(
    @PrimaryKey val compositeId: String, // "$caseId::$evidenceId"
    val caseId: String,
    val evidenceId: String,
    val photographed: Boolean = false,
    val spectralAnalyzed: Boolean = false,
    val collectedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "board_links")
data class BoardLinkEntity(
    @PrimaryKey val id: String, // "$caseId::$evA::$evB"
    val caseId: String,
    val evidenceA: String,
    val evidenceB: String
)

@Entity(tableName = "achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAtMillis: Long = System.currentTimeMillis()
)
