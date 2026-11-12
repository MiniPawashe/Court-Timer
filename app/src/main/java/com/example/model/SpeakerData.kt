package com.example.model

data class SpeakerData(
    val role: SpeakerRole,
    val customName: String = role.defaultName,
    val customRoleLabel: String = role.defaultRoleLabel,
    val accumulatedMillis: Long = 0L,
    val currentTurnStartMillis: Long? = null,
    val turnCount: Int = 0,
    val timeLimitMinutes: Int? = null // null means unlimited
) {
    fun currentElapsedMillis(currentTimeMillis: Long): Long {
        val ongoing = if (currentTurnStartMillis != null) {
            maxOf(0L, currentTimeMillis - currentTurnStartMillis)
        } else {
            0L
        }
        return accumulatedMillis + ongoing
    }

    val isSpeaking: Boolean
        get() = currentTurnStartMillis != null
}

data class SpeechTurn(
    val id: Long = 0L,
    val role: SpeakerRole,
    val speakerName: String,
    val startMillis: Long,
    val endMillis: Long,
    val durationMillis: Long,
    val note: String = ""
)
