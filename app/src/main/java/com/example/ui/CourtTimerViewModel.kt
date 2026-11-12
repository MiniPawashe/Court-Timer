package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CourtDatabase
import com.example.data.CourtSessionEntity
import com.example.data.SpeechTurnEntity
import com.example.model.SpeakerData
import com.example.model.SpeakerRole
import com.example.model.SpeechTurn
import com.example.util.CourtSoundManager
import com.example.util.TimeFormatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourtTimerUiState(
    val caseTitle: String = "State v. Respondent",
    val caseNumber: String = "Case #2026-CV-104",
    val speakers: Map<SpeakerRole, SpeakerData> = mapOf(
        SpeakerRole.JUDGE to SpeakerData(role = SpeakerRole.JUDGE),
        SpeakerRole.PLAINTIFF to SpeakerData(role = SpeakerRole.PLAINTIFF),
        SpeakerRole.DEFENDANT to SpeakerData(role = SpeakerRole.DEFENDANT)
    ),
    val activeSpeaker: SpeakerRole? = null,
    val lastActiveSpeaker: SpeakerRole? = null,
    val turns: List<SpeechTurn> = emptyList(),
    val sessionStartEpochMillis: Long = System.currentTimeMillis(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val soundCuesEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showResetDialog: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val selectedSessionForDetail: CourtSessionEntity? = null,
    val selectedSessionTurns: List<SpeechTurnEntity> = emptyList(),
    val editingSpeaker: SpeakerRole? = null,
    val statusMessage: String? = null,
    val selectedTab: Int = 0 // 0 = Live Timers, 1 = Summary & Distribution
) {
    val isAnyTimerActive: Boolean
        get() = activeSpeaker != null

    fun getSpeakerElapsedMillis(role: SpeakerRole): Long {
        val speaker = speakers[role] ?: return 0L
        return speaker.currentElapsedMillis(currentTimeMillis)
    }

    val totalSpeakingMillis: Long
        get() = SpeakerRole.entries.sumOf { getSpeakerElapsedMillis(it) }

    fun getSpeakerSharePercent(role: SpeakerRole): Float {
        val total = totalSpeakingMillis
        if (total == 0L) return 0f
        return (getSpeakerElapsedMillis(role).toFloat() / total) * 100f
    }
}

class CourtTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CourtDatabase.getInstance(application)
    private val courtDao = db.courtDao()
    val soundManager = CourtSoundManager(application)

    private val _uiState = MutableStateFlow(CourtTimerUiState())
    val uiState: StateFlow<CourtTimerUiState> = _uiState.asStateFlow()

    val pastSessions: StateFlow<List<CourtSessionEntity>> = courtDao.getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var tickerJob: Job? = null
    private var nextTurnId = 1L

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(100L)
                val now = System.currentTimeMillis()
                _uiState.update { it.copy(currentTimeMillis = now) }
                checkTimeLimits(now)
            }
        }
    }

    private var warnedSpeakers = mutableSetOf<SpeakerRole>()
    private var overtimeSpeakers = mutableSetOf<SpeakerRole>()

    private fun checkTimeLimits(now: Long) {
        val active = _uiState.value.activeSpeaker ?: return
        val speaker = _uiState.value.speakers[active] ?: return
        val limitMinutes = speaker.timeLimitMinutes ?: return
        val limitMillis = limitMinutes * 60 * 1000L
        val elapsed = speaker.currentElapsedMillis(now)

        // 1-minute warning (between limit - 60s and limit)
        if (limitMillis > 60000L && elapsed >= (limitMillis - 60000L) && elapsed < limitMillis) {
            if (!warnedSpeakers.contains(active)) {
                warnedSpeakers.add(active)
                soundManager.playWarningTone(_uiState.value.soundCuesEnabled)
                soundManager.triggerHaptic(_uiState.value.hapticFeedbackEnabled, strong = false)
            }
        }

        // Overtime warning
        if (elapsed >= limitMillis) {
            if (!overtimeSpeakers.contains(active)) {
                overtimeSpeakers.add(active)
                soundManager.playOvertimeTone(_uiState.value.soundCuesEnabled)
                soundManager.triggerHaptic(_uiState.value.hapticFeedbackEnabled, strong = true)
            }
        }
    }

    /**
     * Primary action: Easy-to-use toggle for each participant.
     * Only one timer can be active at any time.
     * If tapped speaker is active -> pauses/stops timer.
     * If tapped speaker is inactive -> switches active timer to this speaker immediately.
     */
    fun toggleSpeaker(role: SpeakerRole) {
        val now = System.currentTimeMillis()
        val currentActive = _uiState.value.activeSpeaker

        if (currentActive == role) {
            // Already active -> Pause it
            pauseSpeakerInternal(role, now)
            soundManager.playPauseTone(_uiState.value.soundCuesEnabled)
            soundManager.triggerHaptic(_uiState.value.hapticFeedbackEnabled, strong = false)
        } else {
            // Switching speaker
            if (currentActive != null) {
                pauseSpeakerInternal(currentActive, now)
            }
            startSpeakerInternal(role, now)
            soundManager.playSwitchTone(_uiState.value.soundCuesEnabled)
            soundManager.triggerHaptic(_uiState.value.hapticFeedbackEnabled, strong = true)
        }
    }

    private fun startSpeakerInternal(role: SpeakerRole, timestamp: Long) {
        _uiState.update { state ->
            val speaker = state.speakers[role] ?: return@update state
            val updatedSpeaker = speaker.copy(
                currentTurnStartMillis = timestamp,
                turnCount = speaker.turnCount + 1
            )
            val updatedSpeakers = state.speakers.toMutableMap()
            updatedSpeakers[role] = updatedSpeaker

            state.copy(
                speakers = updatedSpeakers,
                activeSpeaker = role,
                lastActiveSpeaker = role
            )
        }
    }

    private fun pauseSpeakerInternal(role: SpeakerRole, timestamp: Long) {
        _uiState.update { state ->
            val speaker = state.speakers[role] ?: return@update state
            val start = speaker.currentTurnStartMillis ?: timestamp
            val turnDuration = maxOf(0L, timestamp - start)
            val newAccumulated = speaker.accumulatedMillis + turnDuration

            val updatedSpeaker = speaker.copy(
                accumulatedMillis = newAccumulated,
                currentTurnStartMillis = null
            )
            val updatedSpeakers = state.speakers.toMutableMap()
            updatedSpeakers[role] = updatedSpeaker

            // Record turn if duration > 500ms
            val newTurns = if (turnDuration > 500L) {
                val turn = SpeechTurn(
                    id = nextTurnId++,
                    role = role,
                    speakerName = speaker.customName,
                    startMillis = start,
                    endMillis = timestamp,
                    durationMillis = turnDuration
                )
                listOf(turn) + state.turns
            } else {
                state.turns
            }

            state.copy(
                speakers = updatedSpeakers,
                activeSpeaker = null,
                turns = newTurns
            )
        }
    }

    /**
     * Master pause button
     */
    fun pauseAll() {
        val now = System.currentTimeMillis()
        val currentActive = _uiState.value.activeSpeaker
        if (currentActive != null) {
            pauseSpeakerInternal(currentActive, now)
            soundManager.playPauseTone(_uiState.value.soundCuesEnabled)
            soundManager.triggerHaptic(_uiState.value.hapticFeedbackEnabled, strong = false)
        }
    }

    /**
     * Resume either the last active speaker or default to Judge
     */
    fun resumeActiveOrLast() {
        val target = _uiState.value.lastActiveSpeaker ?: SpeakerRole.JUDGE
        toggleSpeaker(target)
    }

    /**
     * Resets the current hearing session timers and turns
     */
    fun resetSession() {
        val now = System.currentTimeMillis()
        warnedSpeakers.clear()
        overtimeSpeakers.clear()

        _uiState.update { state ->
            val resetSpeakers = state.speakers.mapValues { (_, speaker) ->
                speaker.copy(
                    accumulatedMillis = 0L,
                    currentTurnStartMillis = null,
                    turnCount = 0
                )
            }
            state.copy(
                speakers = resetSpeakers,
                activeSpeaker = null,
                lastActiveSpeaker = null,
                turns = emptyList(),
                sessionStartEpochMillis = now,
                showResetDialog = false,
                statusMessage = "Session timers reset"
            )
        }
    }

    fun updateCaseInfo(title: String, number: String) {
        _uiState.update {
            it.copy(caseTitle = title.trim(), caseNumber = number.trim())
        }
    }

    fun updateSpeakerDetails(role: SpeakerRole, newName: String, newRoleLabel: String, timeLimitMin: Int?) {
        _uiState.update { state ->
            val speaker = state.speakers[role] ?: return@update state
            val updated = speaker.copy(
                customName = newName.ifBlank { role.defaultName },
                customRoleLabel = newRoleLabel.ifBlank { role.defaultRoleLabel },
                timeLimitMinutes = if (timeLimitMin != null && timeLimitMin > 0) timeLimitMin else null
            )
            val updatedMap = state.speakers.toMutableMap()
            updatedMap[role] = updated
            state.copy(speakers = updatedMap, editingSpeaker = null)
        }
    }

    fun addNoteToTurn(turnId: Long, note: String) {
        _uiState.update { state ->
            val updated = state.turns.map { turn ->
                if (turn.id == turnId) turn.copy(note = note) else turn
            }
            state.copy(turns = updated)
        }
    }

    fun toggleSoundCues(enabled: Boolean) {
        _uiState.update { it.copy(soundCuesEnabled = enabled) }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        _uiState.update { it.copy(keepScreenOn = enabled) }
    }

    fun setShowResetDialog(show: Boolean) {
        _uiState.update { it.copy(showResetDialog = show) }
    }

    fun setShowSaveDialog(show: Boolean) {
        _uiState.update { it.copy(showSaveDialog = show) }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showHistoryDialog = show) }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setEditingSpeaker(role: SpeakerRole?) {
        _uiState.update { it.copy(editingSpeaker = role) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    /**
     * Saves the current hearing session into the Room database
     */
    fun saveCurrentSession(notes: String) {
        val state = _uiState.value
        val now = System.currentTimeMillis()

        // Pause any active speaker first
        if (state.activeSpeaker != null) {
            pauseSpeakerInternal(state.activeSpeaker, now)
        }

        val judgeElapsed = state.getSpeakerElapsedMillis(SpeakerRole.JUDGE)
        val plaintiffElapsed = state.getSpeakerElapsedMillis(SpeakerRole.PLAINTIFF)
        val defendantElapsed = state.getSpeakerElapsedMillis(SpeakerRole.DEFENDANT)
        val totalMillis = judgeElapsed + plaintiffElapsed + defendantElapsed

        val sessionEntity = CourtSessionEntity(
            caseNumber = state.caseNumber,
            caseTitle = state.caseTitle,
            dateEpochMillis = state.sessionStartEpochMillis,
            judgeName = state.speakers[SpeakerRole.JUDGE]?.customName ?: SpeakerRole.JUDGE.defaultName,
            judgeMillis = judgeElapsed,
            plaintiffName = state.speakers[SpeakerRole.PLAINTIFF]?.customName ?: SpeakerRole.PLAINTIFF.defaultName,
            plaintiffMillis = plaintiffElapsed,
            defendantName = state.speakers[SpeakerRole.DEFENDANT]?.customName ?: SpeakerRole.DEFENDANT.defaultName,
            defendantMillis = defendantElapsed,
            totalSessionMillis = totalMillis,
            totalTurns = state.turns.size,
            notes = notes
        )

        val turnsEntities = state.turns.map { turn ->
            SpeechTurnEntity(
                sessionId = 0L,
                speakerRole = turn.role.name,
                speakerName = turn.speakerName,
                startMillis = turn.startMillis,
                endMillis = turn.endMillis,
                durationMillis = turn.durationMillis,
                note = turn.note
            )
        }

        viewModelScope.launch {
            try {
                courtDao.saveFullSession(sessionEntity, turnsEntities)
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        statusMessage = "Court record saved successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(statusMessage = "Error saving session: ${e.message}")
                }
            }
        }
    }

    fun loadSessionDetails(session: CourtSessionEntity) {
        viewModelScope.launch {
            val turns = courtDao.getTurnsForSession(session.id)
            _uiState.update {
                it.copy(
                    selectedSessionForDetail = session,
                    selectedSessionTurns = turns
                )
            }
        }
    }

    fun clearSessionDetail() {
        _uiState.update {
            it.copy(
                selectedSessionForDetail = null,
                selectedSessionTurns = emptyList()
            )
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            courtDao.deleteSession(sessionId)
            if (_uiState.value.selectedSessionForDetail?.id == sessionId) {
                clearSessionDetail()
            }
        }
    }

    /**
     * Formats an official legal summary text suitable for clipboard copying or email export
     */
    fun generateExportSummary(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.append("COURTROOM SPEAKING TIME RECORD\n")
        sb.append("===================================\n")
        sb.append("Case: ${state.caseTitle} (${state.caseNumber})\n")
        sb.append("Date: ${TimeFormatUtils.formatFullDateTime(state.sessionStartEpochMillis)}\n")
        sb.append("Total Speaking Time: ${TimeFormatUtils.formatMinutesAndSeconds(state.totalSpeakingMillis)} (${TimeFormatUtils.formatDecimalMinutes(state.totalSpeakingMillis)})\n")
        sb.append("Total Speech Turns: ${state.turns.size}\n\n")

        sb.append("PARTICIPANT BREAKDOWN:\n")
        sb.append("-----------------------------------\n")
        SpeakerRole.entries.forEach { role ->
            val speaker = state.speakers[role]
            val elapsed = state.getSpeakerElapsedMillis(role)
            val pct = state.getSpeakerSharePercent(role)
            sb.append("• ${role.title}: ${speaker?.customName}\n")
            sb.append("  Time: ${TimeFormatUtils.formatMinutesAndSeconds(elapsed)} [${TimeFormatUtils.formatDecimalMinutes(elapsed)}] (${String.format("%.1f", pct)}%)\n")
            sb.append("  Turns: ${speaker?.turnCount ?: 0}\n\n")
        }

        if (state.turns.isNotEmpty()) {
            sb.append("TURNS TIMELINE LOG:\n")
            sb.append("-----------------------------------\n")
            state.turns.reversed().forEachIndexed { index, turn ->
                val timeSpan = "${TimeFormatUtils.formatTimestamp(turn.startMillis)} - ${TimeFormatUtils.formatTimestamp(turn.endMillis)}"
                val dur = TimeFormatUtils.formatMinutesAndSeconds(turn.durationMillis)
                val noteStr = if (turn.note.isNotBlank()) " | Note: ${turn.note}" else ""
                sb.append("${index + 1}. [${turn.role.title}] ${turn.speakerName} ($dur) at $timeSpan$noteStr\n")
            }
        }
        return sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        soundManager.release()
    }
}
