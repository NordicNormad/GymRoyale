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

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutLogFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
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

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
                saveWorkoutLog(true)
            } else {
                saveWorkoutLog(false)
            }
            Bonus = false

        }

        // Handle long-press to delete a log entry
        listViewLogs.setOnItemLongClickListener { _, _, position, _ ->
            val logToDelete = workoutLogs[position]  // Get the actual log object
            deleteWorkoutLog(logToDelete)
            true
        }

        return view
    }

    private fun saveWorkoutLog(isBonus: Boolean) {
        val weightStr = editTextWeight.text.toString()
        val repsStr = editTextReps.text.toString()

        if (weightStr.isBlank() || repsStr.isBlank()) {
            Toast.makeText(requireContext(), "Please enter weight and reps", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val weight = weightStr.toDouble()
            val reps = repsStr.toInt()

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            val expMultiplier = 1   // CHANGE THIS TO BALANCE XP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            val maxLogs = csvManager.readWorkoutLogs()
                .sortedWith(compareBy { it.workoutName })
            val newLift =  weight / ((100 - (reps * 2.5)) / 100)
            var maxLiftSpecific = 0.0
            var expPr = 0.0
            for ((index, log) in maxLogs.withIndex()) {
                if (log.workoutName == workoutName) {
                    if (log.weight / ((100 - (log.reps * 2.5)) / 100) > maxLiftSpecific) {
                        maxLiftSpecific = log.weight / ((100 - (log.reps * 2.5)) / 100)
                    }
                }
                if (index == maxLogs.size - 1) {
                    if (newLift > maxLiftSpecific) {
                        expPr = (newLift - maxLiftSpecific + 100) * expMultiplier
//                        Toast.makeText(requireContext(), "XP +$expPr (PR)", Toast.LENGTH_SHORT).show()
                    } else {
                        expPr = (newLift - maxLiftSpecific + 100) * expMultiplier
                        if (expPr < 0) {
                            expPr = 0.0
                        }
//                        Toast.makeText(requireContext(), "XP +$expPr (NO PR)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Generate a unique ID (could be more sophisticated in a real app)
            val logs = csvManager.readWorkoutLogs().filter { it.workoutName == workoutName }

            val workoutLog = WorkoutLog(
                workoutName = workoutName,
                weight = weight,
                reps = reps,
                xp = expPr.toInt()
            )

            // Save the workout log to CSV
            csvManager.addWorkoutLog(workoutLog)

            // Increment XP in Firebase
            incrementUserXP(isBonus, expPr.toInt())

            // Reload logs
            loadExistingLogs()

        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid weight or reps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementUserXP(isBonus: Boolean, addXP: Int) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    var currentXP = document.getLong("xp")?.toInt() ?: 0
                    var toastString = ""
                    if (isBonus) {
                        currentXP += addXP + 50
                        toastString = "${addXP+50} XP added (Bonus!)"
                    } else {
                        currentXP += addXP
                        toastString = "$addXP XP added"
                    }

                    // Update XP in Firestore
                    firestore.collection("users").document(userId)
                        .update("xp", currentXP)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), toastString, Toast.LENGTH_SHORT).show()
                            Log.d("WorkoutLogFragment", "XP successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("WorkoutLogFragment", "Error updating XP", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("WorkoutLogFragment", "Error retrieving user data", e)
            }
    }

    private fun loadExistingLogs() {
        // Load all logs for the current workout and update the list
        val logs = csvManager.readWorkoutLogs()
            .filter { it.workoutName == workoutName }
            .sortedByDescending { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) }
            .sortedByDescending { it.timestamp }

        workoutLogs.clear()
        workoutLogs.addAll(logs)

        // Display log entries in the format "date --- weight lbs --- reps reps"
        val logDisplayList = logs.map { "${it.date} --- ${it.weight} lbs --- ${it.reps} reps" }
        logAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, logDisplayList)
        listViewLogs.adapter = logAdapter
        logAdapter.notifyDataSetChanged()
    }

    private fun deleteWorkoutLog(log: WorkoutLog) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    var currentXP = document.getLong("xp")?.toInt() ?: 0

                    // Subtract the log's XP from the current XP
                    currentXP -= log.xp

                    // Ensure XP doesn't go below zero
                    if (currentXP < 0) {
                        currentXP = 0
                    }

                    // Update XP in Firebase
                    firestore.collection("users").document(userId)
                        .update("xp", currentXP)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Deletion: -${log.xp} XP", Toast.LENGTH_SHORT).show()
                            Log.d("WorkoutLogFragment", "XP successfully updated after log deletion")
                        }
                        .addOnFailureListener { e ->
                            Log.w("WorkoutLogFragment", "Error updating XP after log deletion", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("WorkoutLogFragment", "Error retrieving user data", e)
            }

        // Delete the log from the CSV
        csvManager.deleteWorkoutLog(log.workoutName, log.date, log.timestamp)

        // Reload the logs to refresh the list
        loadExistingLogs()
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
