package com.example.jc_time

import java.io.Serializable // To pass it via Intent if needed, or for file saving
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class TimeLogEntry(
    val date: String,
    val totalSeconds: Int
) : Serializable {

    // Helper to format the totalSeconds into H:M:S for display
    fun getFormattedTime(): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d H %d M %d S", hours, minutes, seconds)
        } else if (minutes > 0) {
            String.format(Locale.getDefault(), "%d M %d S", minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d S", seconds)
        }
    }
}