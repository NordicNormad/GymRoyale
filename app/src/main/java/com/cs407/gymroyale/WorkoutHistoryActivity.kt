package com.cs407.gymroyale

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.gymroyale.R
import com.cs407.gymroyale.WorkoutLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var listViewWorkoutHistory: ListView
    private lateinit var buttonCancel: Button
    private lateinit var workoutHistoryAdapter: ArrayAdapter<String>
    private val workoutHistory = mutableListOf<String>()

    // Firebase references
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history)

        listViewWorkoutHistory = findViewById(R.id.listViewWorkoutHistory)
        buttonCancel = findViewById(R.id.buttonCancel)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load the workout history from Firebase
        loadWorkoutHistory()

        // Set up Cancel button to go back to the SearchWorkout activity
        buttonCancel.setOnClickListener {
            finish()  // Close this activity and return to SearchWorkout screen
        }
    }

    private fun loadWorkoutHistory() {
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) { readWorkoutLogsFromFirebase() }

            workoutHistory.clear()

            if (logs.isEmpty()) {
                // If no workout logs, show "Empty Workout Log"
                workoutHistory.add("Empty Workout Log")
            } else {
                // Sort logs by workout name, then by date, and then by timestamp
                val sortedLogs = logs.sortedWith(compareBy({ it.workoutName }, { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }, { it.timestamp }))

                // Group logs by workout name
                val groupedLogs = sortedLogs.groupBy { it.workoutName }

                groupedLogs.forEach { (workoutName, workoutLogs) ->
                    // Add workout name as a header
                    workoutHistory.add("--- $workoutName ---")

                    // Sort logs within each workout group by most recent date, then by timestamp
                    val sortedWorkoutLogs = workoutLogs.sortedWith(compareByDescending<WorkoutLog> {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                    }.thenByDescending { it.timestamp })

                    // Add individual logs for this workout
                    sortedWorkoutLogs.forEach { log ->
                        workoutHistory.add("${log.date} --- ${log.weight} lbs --- ${log.reps} reps")
                    }

                    // Add a blank line between different workouts
                    workoutHistory.add("")
                }
            }

            // Update ListView with workout history
            workoutHistoryAdapter = object : ArrayAdapter<String>(
                this@WorkoutHistoryActivity,
                android.R.layout.simple_list_item_1,
                workoutHistory
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.WHITE)
                    textView.typeface = ResourcesCompat.getFont(context, R.font.pixel_font)
                    return view
                }
            }
            listViewWorkoutHistory.adapter = workoutHistoryAdapter
        }
    }

    private suspend fun readWorkoutLogsFromFirebase(): List<WorkoutLog> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val workoutLogsList = document.get("workoutlog") as? List<Map<String, Any>> ?: emptyList()

            workoutLogsList.map {
                // Safely cast weight to Double
                val weight = when (val weightValue = it["weight"]) {
                    is Long -> weightValue.toDouble()  // Convert Long to Double if necessary
                    is Double -> weightValue
                    else -> 0.0  // Default to 0.0 if the value is neither Long nor Double
                }

                WorkoutLog(
                    workoutName = it["workoutName"] as String,
                    weight = weight,
                    reps = (it["reps"] as Long).toInt(),
                    xp = (it["xp"] as Long).toInt(),
                    timestamp = it["timestamp"] as Long,
                    date = it["date"] as String
                )
            }
        } catch (e: Exception) {
            Log.e("WorkoutHistoryActivity", "Error fetching workout logs", e)
            emptyList()
        }
    }

}
