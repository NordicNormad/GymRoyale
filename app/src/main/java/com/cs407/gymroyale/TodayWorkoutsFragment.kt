package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayWorkoutsFragment : Fragment() {

    private lateinit var listViewTodayWorkouts: ListView
    private lateinit var textViewNoWorkouts: TextView
    private lateinit var csvManager: WorkoutLogCSVManager
    private lateinit var todayWorkoutAdapter: ArrayAdapter<String>
    private val todayWorkouts = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today_workouts, container, false)

        listViewTodayWorkouts = view.findViewById(R.id.listViewTodayWorkouts)
        textViewNoWorkouts = view.findViewById(R.id.textViewNoWorkouts)

        csvManager = WorkoutLogCSVManager(requireContext())

        todayWorkoutAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            todayWorkouts
        )
        listViewTodayWorkouts.adapter = todayWorkoutAdapter

        loadTodayWorkouts()

        return view
    }

    private fun loadTodayWorkouts() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logs = csvManager.readWorkoutLogs()
            .filter { it.date == today }
            .sortedWith(compareBy({ it.workoutName }, { it.id }))

        todayWorkouts.clear()

        // Group logs by workout name
        val groupedLogs = logs.groupBy { it.workoutName }

        groupedLogs.forEach { (workoutName, workoutLogs) ->
            // Add workout name as a header
            todayWorkouts.add("--- $workoutName ---")

            val sortedLogs = workoutLogs.sortedByDescending { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }
            // Add individual logs for this workout
            sortedLogs.forEach { log ->
                todayWorkouts.add("${log.weight} lbs --- ${log.reps} reps")
            }

            // Add a blank line between different workouts
            todayWorkouts.add("")
        }

        todayWorkoutAdapter.notifyDataSetChanged()

        // Show/hide no workouts message
        if (todayWorkouts.isEmpty()) {
            textViewNoWorkouts.visibility = View.VISIBLE
            listViewTodayWorkouts.visibility = View.GONE
        } else {
            textViewNoWorkouts.visibility = View.GONE
            listViewTodayWorkouts.visibility = View.VISIBLE
        }
    }
}