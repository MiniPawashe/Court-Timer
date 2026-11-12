package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CourtSessionEntity::class, SpeechTurnEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CourtDatabase : RoomDatabase() {
    abstract fun courtDao(): CourtDao

    companion object {
        @Volatile
        private var INSTANCE: CourtDatabase? = null

        fun getInstance(context: Context): CourtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CourtDatabase::class.java,
                    "court_timer.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
