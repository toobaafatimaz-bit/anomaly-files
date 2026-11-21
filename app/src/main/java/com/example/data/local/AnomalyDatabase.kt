package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        CaseProgressEntity::class,
        CollectedEvidenceEntity::class,
        BoardLinkEntity::class,
        UnlockedAchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnomalyDatabase : RoomDatabase() {
    abstract fun anomalyDao(): AnomalyDao

    companion object {
        @Volatile
        private var INSTANCE: AnomalyDatabase? = null

        fun getInstance(context: Context): AnomalyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AnomalyDatabase::class.java,
                    "anomaly_files.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
