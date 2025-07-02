package com.example.jc_time

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.* // Import for file operations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var timeLabel: TextView
    private lateinit var timeButton: Button
    private lateinit var viewtimeLogsButton: Button

    private var running: Boolean = false
    private var lastRecordedTotalSeconds: Int = 0 // Store the last recorded time

    // Define the filename for your logs
    private val LOG_FILENAME = "time_logs.txt" // Or .json if you use a JSON library

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timeLabel = findViewById(R.id.timeLabel)
        timeButton = findViewById(R.id.timeButton)
        viewtimeLogsButton = findViewById(R.id.viewTimeLogsButton)

        // Set initial text for the button and label
        timeButton.text = getString(R.string.start_timer_button_text)
        timeLabel.text = getString(R.string.timer_stopped_message)

        timeButton.setOnClickListener {
            // Toggle the running state
            running = !running

            if (running) {
                startTimer() // Start the timer if it's now 'running'
                timeButton.text = getString(R.string.stop_timer_button_text)
                Toast.makeText(this, getString(R.string.timer_running_message), Toast.LENGTH_SHORT).show()
            } else {
                // When 'running' becomes false, the while loop in startTimer will exit on its next iteration
                timeButton.text = getString(R.string.start_timer_button_text)
                timeLabel.text = getString(R.string.timer_stopped_message)
                Toast.makeText(this, getString(R.string.timer_stopping_toast), Toast.LENGTH_SHORT).show()

                // Save time log here when the timer is stopped
                saveTimeLog(lastRecordedTotalSeconds) // Use the stored value
            }
        }

        viewtimeLogsButton.setOnClickListener {
            // Switch Activity Views to LogTimeActivity
            val intent = Intent(this, ActivityLogTime::class.java)
            // No need to pass the file path if LogTimeActivity can read from the same known file
            startActivity(intent)
        }
    }

    private fun startTimer() {
        lifecycleScope.launch {
            var currentSessionSeconds: Int = 0 // Track seconds for the current session
            while (running) {
                currentSessionSeconds++
                lastRecordedTotalSeconds = currentSessionSeconds // Update the class-level variable

                val hours = currentSessionSeconds / 3600
                val minutes = (currentSessionSeconds % 3600) / 60
                val seconds = currentSessionSeconds % 60

                if (hours > 0) {
                    timeLabel.text = getString(R.string.time_format_hours_minutes_seconds, hours, minutes, seconds)
                } else if (minutes > 0) {
                    timeLabel.text = getString(R.string.time_format_minutes_seconds, minutes, seconds)
                } else {
                    timeLabel.text = getString(R.string.time_format_seconds, seconds)
                }

                delay(1000) // Delay for 1 second
            }
        }
    }

    // Function to save a new time log entry
    private fun saveTimeLog(totalSeconds: Int) {
        // Only save if the timer actually ran for at least one second
        if (totalSeconds <= 0) {
            Toast.makeText(this, "Timer ran for 0 seconds, not saving.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Create the log entry
        val newLogEntry = TimeLogEntry(currentDate, totalSeconds)

        // Get the file path in internal storage
        val file = File(filesDir, LOG_FILENAME)

        try {
            // Append the new log entry to the file
            // Use BufferedWriter for efficient writing
            BufferedWriter(FileWriter(file, true)).use { writer -> // 'true' for append mode
                // Simple CSV-like format: Date,TotalSeconds
                writer.append("${newLogEntry.date},${newLogEntry.totalSeconds}")
                writer.newLine() // Add a new line for the next entry
            }
            Toast.makeText(this, "Time log saved!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error saving time log: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // Function to load all time log entries
    private fun loadTimeLogs(): MutableList<TimeLogEntry> {
        val logs = mutableListOf<TimeLogEntry>()
        val file = File(filesDir, LOG_FILENAME)

        if (!file.exists()) {
            return logs // Return empty list if file doesn't exist
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
                                // Handle malformed line
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error loading time logs: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return logs
    }
}