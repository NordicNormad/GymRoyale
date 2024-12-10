package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TodayWorkoutsFragment : Fragment() {

    private lateinit var listViewTodayWorkouts: ListView
    private lateinit var textViewNoWorkouts: TextView
    private lateinit var todayWorkoutAdapter: ArrayAdapter<String>
    private val todayWorkouts = mutableListOf<String>()

    // Firebase references
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today_workouts, container, false)

        listViewTodayWorkouts = view.findViewById(R.id.listViewTodayWorkouts)
        textViewNoWorkouts = view.findViewById(R.id.textViewNoWorkouts)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        todayWorkoutAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            todayWorkouts
        )
        listViewTodayWorkouts.adapter = todayWorkoutAdapter

        // Load today's workouts from Firebase
        loadTodayWorkouts()

        return view
    }

    private fun loadTodayWorkouts() {
        lifecycleScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val logs = withContext(Dispatchers.IO) { readTodayWorkoutLogsFromFirebase(today) }

            todayWorkouts.clear()

            if (logs.isEmpty()) {
                // Show "No Workouts" message if no logs for today
                textViewNoWorkouts.visibility = View.VISIBLE
                listViewTodayWorkouts.visibility = View.GONE
            } else {
                // Group logs by workout name
                val groupedLogs = logs.groupBy { it.workoutName }

                groupedLogs.forEach { (workoutName, workoutLogs) ->
                    // Add workout name as a header
                    todayWorkouts.add("--- $workoutName ---")

                    val sortedLogs = workoutLogs.sortedByDescending {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                    }

                    // Add individual logs for this workout
                    sortedLogs.forEach { log ->
                        todayWorkouts.add("${log.weight} lbs --- ${log.reps} reps")
                    }

                    // Add a blank line between different workouts
                    todayWorkouts.add("")
                }

                todayWorkoutAdapter.notifyDataSetChanged()

                // Show list if there are logs
                textViewNoWorkouts.visibility = View.GONE
                listViewTodayWorkouts.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun readTodayWorkoutLogsFromFirebase(today: String): List<WorkoutLog> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val workoutLogsList = document.get("workoutlog") as? List<Map<String, Any>> ?: emptyList()

            workoutLogsList.mapNotNull { log ->
                val logDate = log["date"] as String
                if (logDate == today) {
                    WorkoutLog(
                        workoutName = log["workoutName"] as String,
                        weight = log["weight"] as Double,
                        reps = (log["reps"] as Long).toInt(),
                        xp = (log["xp"] as Long).toInt(),
                        timestamp = log["timestamp"] as Long,
                        date = log["date"] as String
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            // Log error and return empty list if there's an issue fetching data
            emptyList()
        }
    }
}
