package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "court_sessions")
data class CourtSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val caseNumber: String,
    val caseTitle: String,
    val dateEpochMillis: Long,
    val judgeName: String,
    val judgeMillis: Long,
    val plaintiffName: String,
    val plaintiffMillis: Long,
    val defendantName: String,
    val defendantMillis: Long,
    val totalSessionMillis: Long,
    val totalTurns: Int,
    val notes: String = ""
)
