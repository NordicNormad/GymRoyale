package com.cs407.gymroyale

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutLogFragment : Fragment() {

    private lateinit var textViewWorkoutName: TextView
    private lateinit var textViewBounty: TextView
    private lateinit var editTextWeight: EditText
    private lateinit var editTextReps: EditText
    private lateinit var buttonSave: Button
    private lateinit var listViewLogs: ListView
    private lateinit var csvManager: WorkoutLogCSVManager
    private lateinit var workoutName: String
    private lateinit var logAdapter: ArrayAdapter<String>
    private val workoutLogs = mutableListOf<WorkoutLog>()  // Store actual log objects

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_log, container, false)
        // Retrieve workout name from arguments
        workoutName = arguments?.getString("WORKOUT_NAME") ?: "Unknown Workout"
        val isBounty = activity?.intent?.getBooleanExtra("IS_BOUNTY", false) ?: false
        var Bonus = true
        activity?.intent?.removeExtra("IS_BOUNTY")
        textViewWorkoutName = view.findViewById(R.id.textViewWorkoutName)
        editTextWeight = view.findViewById(R.id.editTextWeight)
        editTextReps = view.findViewById(R.id.editTextReps)
        buttonSave = view.findViewById(R.id.buttonSave)
        listViewLogs = view.findViewById(R.id.listViewLogs)
        textViewBounty = view.findViewById(R.id.textViewBounty)


        // Set workout name
        textViewWorkoutName.text = workoutName
        if (isBounty) {
            Log.d("WorkoutLogFragment", "Setting bounty label to VISIBLE")
            textViewBounty.visibility = View.VISIBLE
        } else {
            textViewBounty.visibility = View.GONE
        }

        csvManager = WorkoutLogCSVManager(requireContext())
        // Load existing logs and set up adapter
        loadExistingLogs()

        buttonSave.setOnClickListener {
            textViewBounty.visibility = View.INVISIBLE
            if (isBounty && Bonus) {
                Toast.makeText(requireContext(), "Bonus XP added", Toast.LENGTH_SHORT).show()
            }
            Bonus = false
            saveWorkoutLog()
        }

        // Handle long-press to delete a log entry
        listViewLogs.setOnItemLongClickListener { _, _, position, _ ->
            val logToDelete = workoutLogs[position]  // Get the actual log object
            deleteWorkoutLog(logToDelete)
            true
        }

        return view
    }

    private fun saveWorkoutLog() {
        val weightStr = editTextWeight.text.toString()
        val repsStr = editTextReps.text.toString()

        if (weightStr.isBlank() || repsStr.isBlank()) {
            Toast.makeText(requireContext(), "Please enter weight and reps", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val weight = weightStr.toDouble()
            val reps = repsStr.toInt()

            // Generate a unique ID (could be more sophisticated in a real app)
            val logs = csvManager.readWorkoutLogs().filter { it.workoutName == workoutName }
            val newId = if (logs.isEmpty()) 1 else logs.maxOf { it.id } + 1

            val workoutLog = WorkoutLog(
                id = newId,
                workoutName = workoutName,
                weight = weight,
                reps = reps
            )

            csvManager.addWorkoutLog(workoutLog)
            loadExistingLogs()

        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid weight or reps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExistingLogs() {
        // Load all logs for the current workout and update the list
        val logs = csvManager.readWorkoutLogs()
            .filter { it.workoutName == workoutName }
            .sortedByDescending { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }

        workoutLogs.clear()
        workoutLogs.addAll(logs)

        // Display log entries in the format "date --- weight lbs --- reps reps"
        val logDisplayList = logs.map { "${it.date} --- ${it.weight} lbs --- ${it.reps} reps" }
        logAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, logDisplayList)
        listViewLogs.adapter = logAdapter
        logAdapter.notifyDataSetChanged()
    }


    private fun deleteWorkoutLog(log: WorkoutLog) {
        // Use the workout name and date to delete the correct log
        csvManager.deleteWorkoutLog(log.workoutName, log.date, log.id)
        loadExistingLogs()  // Refresh the list
    }


    companion object {
        fun newInstance(workoutName: String): WorkoutLogFragment {
            val fragment = WorkoutLogFragment()
            val args = Bundle()
            args.putString("WORKOUT_NAME", workoutName)
            fragment.arguments = args
            return fragment
        }
    }
}