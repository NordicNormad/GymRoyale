package com.cs407.gymroyale

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var listViewWorkoutHistory: ListView
    private lateinit var buttonCancel: Button
    private lateinit var workoutHistoryAdapter: ArrayAdapter<String>
    private val workoutHistory = mutableListOf<String>()
    private lateinit var csvManager: WorkoutLogCSVManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history)

        listViewWorkoutHistory = findViewById(R.id.listViewWorkoutHistory)
        buttonCancel = findViewById(R.id.buttonCancel)

        csvManager = WorkoutLogCSVManager(this)

        // Load the workout history
        loadWorkoutHistory()

        // Set up Cancel button to go back to the SearchWorkout activity
        buttonCancel.setOnClickListener {
            finish()  // Close this activity and return to SearchWorkout screen
        }
    }

    private fun loadWorkoutHistory() {
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) { csvManager.readWorkoutLogs() }
                .sortedWith(compareBy({ it.workoutName }, { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }))

            workoutHistory.clear()

            // Group logs by workout name
            val groupedLogs = logs.groupBy { it.workoutName }

            groupedLogs.forEach { (workoutName, workoutLogs) ->
                // Add workout name as a header
                workoutHistory.add("--- $workoutName ---")

                // Sort logs within each workout group by most recent date
                val sortedLogs = workoutLogs.sortedByDescending { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }

                // Add individual logs for this workout
                sortedLogs.forEach { log ->
                    workoutHistory.add("${log.date} --- ${log.weight} lbs --- ${log.reps} reps")
                }

                // Add a blank line between different workouts
                workoutHistory.add("")
            }

            // Update ListView with workout history
            workoutHistoryAdapter = ArrayAdapter(
                this@WorkoutHistoryActivity,
                android.R.layout.simple_list_item_1,
                workoutHistory
            )
            listViewWorkoutHistory.adapter = workoutHistoryAdapter
        }
    }

}
