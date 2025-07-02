// Create a new file: app/src/main/java/com/example/jc_time/TimeLogRepository.kt
package com.example.jc_time

import android.content.Context
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeLogRepository(private val context: Context) {

    private val LOG_FILENAME = "time_logs.txt"

    // Save a new time log entry
    fun saveTimeLog(totalSeconds: Int) {
        if (totalSeconds <= 0) {
            return // Don't save empty logs
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val newLogEntry = TimeLogEntry(currentDate, totalSeconds)

        val file = File(context.filesDir, LOG_FILENAME)

        try {
            BufferedWriter(FileWriter(file, true)).use { writer ->
                writer.append("${newLogEntry.date},${newLogEntry.totalSeconds}")
                writer.newLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteTimeLogs() {
        val file = File(context.filesDir, LOG_FILENAME)

        if (file.exists()) {
            try {
                file.delete()
            }
            catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Load all time log entries
    fun loadTimeLogs(): List<TimeLogEntry> {
        val logs = mutableListOf<TimeLogEntry>()
        val file = File(context.filesDir, LOG_FILENAME)

        if (!file.exists()) {
            return logs
        }

        try {
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let {
                        val parts = it.split(",")
                        if (parts.size == 2) {
                            try {
                                val date = parts[0]
                                val totalSeconds = parts[1].toInt()
                                logs.add(TimeLogEntry(date, totalSeconds))
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return logs
    }
}