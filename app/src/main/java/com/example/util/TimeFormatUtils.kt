package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatUtils {

    /**
     * Formats milliseconds into standard MM:SS or HH:MM:SS
     */
    fun formatClockTime(millis: Long): String {
        val positive = maxOf(0L, millis)
        val hours = TimeUnit.MILLISECONDS.toHours(positive)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(positive) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(positive) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats milliseconds into minutes and seconds with units, e.g. "12m 45s" or "1h 12m"
     */
    fun formatMinutesAndSeconds(millis: Long): String {
        val positive = maxOf(0L, millis)
        val hours = TimeUnit.MILLISECONDS.toHours(positive)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(positive) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(positive) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Formats milliseconds into precise fractional minutes, e.g. "12.75 min"
     * Often needed in legal billing, depositions, and trial records.
     */
    fun formatDecimalMinutes(millis: Long): String {
        val positive = maxOf(0L, millis)
        val decimalMinutes = positive / 60000.0
        return String.format(Locale.getDefault(), "%.2f min", decimalMinutes)
    }

    /**
     * Formats a timestamp into friendly time, e.g. "10:42:15 AM"
     */
    fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    /**
     * Formats a date timestamp, e.g. "Oct 5, 2026 • 10:42 AM"
     */
    fun formatFullDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
