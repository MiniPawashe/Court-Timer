package com.example

import com.example.model.SpeakerData
import com.example.model.SpeakerRole
import com.example.ui.CourtTimerUiState
import com.example.util.TimeFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFormatMinutesAndSeconds() {
        // 90 seconds = 1m 30s
        assertEquals("1m 30s", TimeFormatUtils.formatMinutesAndSeconds(90000L))
        // 45 seconds = 45s
        assertEquals("45s", TimeFormatUtils.formatMinutesAndSeconds(45000L))
        // 75 minutes = 1h 15m 0s
        assertEquals("1h 15m 0s", TimeFormatUtils.formatMinutesAndSeconds(75 * 60 * 1000L))
    }

    @Test
    fun testFormatClockTime() {
        assertEquals("01:30", TimeFormatUtils.formatClockTime(90000L))
        assertEquals("14:25", TimeFormatUtils.formatClockTime(865000L))
        assertEquals("01:05:00", TimeFormatUtils.formatClockTime(65 * 60 * 1000L))
    }

    @Test
    fun testFormatDecimalMinutes() {
        // 30 seconds = 0.50 min
        assertEquals("0.50 min", TimeFormatUtils.formatDecimalMinutes(30000L))
        // 90 seconds = 1.50 min
        assertEquals("1.50 min", TimeFormatUtils.formatDecimalMinutes(90000L))
        // 14 min 15 sec = 14.25 min
        assertEquals("14.25 min", TimeFormatUtils.formatDecimalMinutes(855000L))
    }

    @Test
    fun testSpeakerDataElapsedCalculation() {
        val speaker = SpeakerData(
            role = SpeakerRole.JUDGE,
            accumulatedMillis = 60000L,
            currentTurnStartMillis = 100000L
        )

        assertTrue(speaker.isSpeaking)
        // If current time is 115000L, elapsed is 60000 + 15000 = 75000L
        assertEquals(75000L, speaker.currentElapsedMillis(115000L))

        val pausedSpeaker = speaker.copy(currentTurnStartMillis = null)
        assertFalse(pausedSpeaker.isSpeaking)
        assertEquals(60000L, pausedSpeaker.currentElapsedMillis(115000L))
    }

    @Test
    fun testSummaryTotalsAndDistributionShare() {
        // 10 mins Judge (600,000ms), 20 mins Plaintiff (1,200,000ms), 20 mins Defendant (1,200,000ms)
        // Total = 50 mins (3,000,000ms)
        val uiState = CourtTimerUiState(
            speakers = mapOf(
                SpeakerRole.JUDGE to SpeakerData(role = SpeakerRole.JUDGE, accumulatedMillis = 600000L),
                SpeakerRole.PLAINTIFF to SpeakerData(role = SpeakerRole.PLAINTIFF, accumulatedMillis = 1200000L),
                SpeakerRole.DEFENDANT to SpeakerData(role = SpeakerRole.DEFENDANT, accumulatedMillis = 1200000L)
            ),
            currentTimeMillis = 0L
        )

        assertEquals(3000000L, uiState.totalSpeakingMillis)
        assertEquals("50m 0s", TimeFormatUtils.formatMinutesAndSeconds(uiState.totalSpeakingMillis))
        assertEquals("50.00 min", TimeFormatUtils.formatDecimalMinutes(uiState.totalSpeakingMillis))

        assertEquals(20.0f, uiState.getSpeakerSharePercent(SpeakerRole.JUDGE), 0.01f)
        assertEquals(40.0f, uiState.getSpeakerSharePercent(SpeakerRole.PLAINTIFF), 0.01f)
        assertEquals(40.0f, uiState.getSpeakerSharePercent(SpeakerRole.DEFENDANT), 0.01f)
    }
}
