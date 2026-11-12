package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "speech_turns",
    foreignKeys = [
        ForeignKey(
            entity = CourtSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SpeechTurnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val speakerRole: String, // "JUDGE", "PLAINTIFF", "DEFENDANT"
    val speakerName: String,
    val startMillis: Long,
    val endMillis: Long,
    val durationMillis: Long,
    val note: String = ""
)
