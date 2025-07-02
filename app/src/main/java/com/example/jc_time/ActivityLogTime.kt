package com.example.jc_time

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope // For coroutines if needed
import kotlinx.coroutines.launch

class ActivityLogTime : AppCompatActivity() {
    private lateinit var backButton: Button
    private lateinit var clearLogsButton: Button
    private lateinit var logTextView: TextView // This will display the logs

    private lateinit var timeLogRepository: TimeLogRepository // Add this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_time)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.backButton)
        clearLogsButton = findViewById(R.id.clearLogsButton)
        logTextView = findViewById(R.id.logTextView)

        // Initialize the repository
        timeLogRepository = TimeLogRepository(applicationContext)

        // Load and display logs when the activity is created
        displayLogs()

        backButton.setOnClickListener {
            finish()
        }

        clearLogsButton.setOnClickListener {
            clearLogs()
        }
    }

    private fun displayLogs() {
        lifecycleScope.launch { // Launch a coroutine for file I/O
            val logs = timeLogRepository.loadTimeLogs()
            if (logs.isEmpty()) {
                logTextView.text = getString(R.string.no_logs_available)
            } else {
                val stringBuilder = StringBuilder()
                logs.forEach { log ->
                    stringBuilder.append("Date: ${log.date}, Time: ${log.getFormattedTime()}\n")
                }
                logTextView.text = stringBuilder.toString()
            }
        }
    }

    private fun clearLogs() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Clear Logs")
        builder.setPositiveButton("Continue") {dialog, which ->
            Toast.makeText(applicationContext, "Deleting all log entries...", Toast.LENGTH_SHORT).show()

            val logs = timeLogRepository.loadTimeLogs()
            if (logs.isEmpty()) {
                Toast.makeText(applicationContext, "No time logs to delete...", Toast.LENGTH_SHORT).show()
            }
            else {
                timeLogRepository.deleteTimeLogs()

                logTextView.text = getString(R.string.no_logs_available)
            }

        }
        builder.setNegativeButton("Cancel") {dialog, which ->
            Toast.makeText(applicationContext, "Cancelled Operation", Toast.LENGTH_SHORT).show()
        }
        builder.setOnCancelListener {
            Toast.makeText(applicationContext, "Cancelled Operation", Toast.LENGTH_SHORT).show()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}