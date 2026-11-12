package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class SessionWithTurns(
    val session: CourtSessionEntity,
    val turns: List<SpeechTurnEntity>
)

@Dao
interface CourtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CourtSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurns(turns: List<SpeechTurnEntity>)

    @Query("SELECT * FROM court_sessions ORDER BY dateEpochMillis DESC")
    fun getAllSessionsFlow(): Flow<List<CourtSessionEntity>>

    @Query("SELECT * FROM speech_turns WHERE sessionId = :sessionId ORDER BY startMillis ASC")
    suspend fun getTurnsForSession(sessionId: Long): List<SpeechTurnEntity>

    @Query("DELETE FROM court_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Transaction
    suspend fun saveFullSession(
        session: CourtSessionEntity,
        turns: List<SpeechTurnEntity>
    ): Long {
        val sessionId = insertSession(session)
        val linkedTurns = turns.map { it.copy(sessionId = sessionId) }
        insertTurns(linkedTurns)
        return sessionId
    }
}
