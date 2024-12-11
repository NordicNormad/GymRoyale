package com.cs407.gymroyale

import android.graphics.Color
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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class WorkoutLogFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var textViewWorkoutName: TextView
    private lateinit var textViewBounty: TextView
    private lateinit var editTextWeight: EditText
    private lateinit var editTextReps: EditText
    private lateinit var buttonSave: Button
    private lateinit var listViewLogs: ListView
    private lateinit var workoutName: String
    private lateinit var logAdapter: ArrayAdapter<String>
    private val workoutLogs = mutableListOf<WorkoutLog>()  // Store actual log objects

    // Add this function
    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_log, container, false)

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        workoutName = arguments?.getString("WORKOUT_NAME") ?: "Unknown Workout"
        val isBounty = activity?.intent?.getBooleanExtra("IS_BOUNTY", false) ?: false
        var Bonus = true

        textViewWorkoutName = view.findViewById(R.id.textViewWorkoutName)
        editTextWeight = view.findViewById(R.id.editTextWeight)
        editTextReps = view.findViewById(R.id.editTextReps)
        buttonSave = view.findViewById(R.id.buttonSave)
        listViewLogs = view.findViewById(R.id.listViewLogs)
        textViewBounty = view.findViewById(R.id.textViewBounty)

        textViewWorkoutName.text = workoutName
        textViewBounty.visibility = if (isBounty) View.VISIBLE else View.GONE

        loadExistingLogs() // Loads logs from Firebase

        buttonSave.setOnClickListener {
            textViewBounty.visibility = View.INVISIBLE
            hideKeyboard()
            if (isBounty && Bonus) {
                saveWorkoutLog(true)
            } else {
                saveWorkoutLog(false)
            }
            Bonus = false

        }

        // Handle long-press to delete a log
        listViewLogs.setOnItemLongClickListener { _, _, position, _ ->
            val logToDelete = workoutLogs[position]
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
            val timestamp = System.currentTimeMillis()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Calculate XP logic (same as before)
            val expMultiplier = 1
            fetchMaxLog { maxLiftSpecific ->
                val newLift = weight / ((100 - (reps * 2.5)) / 100)
                val expPr = maxOf((newLift - maxLiftSpecific + 100) * expMultiplier, 0.0).toInt()

                // Save the workout log to Firebase
                val workoutLog = mapOf(
                    "workoutName" to workoutName,
                    "weight" to weight,
                    "reps" to reps,
                    "date" to date,
                    "timestamp" to timestamp,
                    "xp" to expPr
                )

                val userId = auth.currentUser?.uid ?: return@fetchMaxLog

                firestore.collection("users").document(userId)
                    .update("workoutlog", FieldValue.arrayUnion(workoutLog))
                    .addOnSuccessListener {
                        incrementUserXP(isBonus, expPr)
                        loadExistingLogs() // Reload logs after save
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save log.", Toast.LENGTH_SHORT).show()
                    }
            }

        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid weight or reps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMaxLog(callback: (Double) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val workoutLogs = document.get("workoutlog") as? List<Map<String, Any>> ?: emptyList()

                val maxLog = workoutLogs
                    .filter { it["workoutName"] == workoutName }
                    .maxByOrNull {
                        val weight = (it["weight"] as? Number)?.toDouble() ?: 0.0
                        val reps = (it["reps"] as? Number)?.toDouble() ?: 0.0
                        weight / ((100 - (reps * 2.5)) / 100)
                    }

                val maxLiftSpecific = maxLog?.let {
                    val weight = (it["weight"] as? Number)?.toDouble() ?: 0.0
                    val reps = (it["reps"] as? Number)?.toDouble() ?: 0.0
                    weight / ((100 - (reps * 2.5)) / 100)
                } ?: 0.0

                callback(maxLiftSpecific)
            }
            .addOnFailureListener {
                callback(0.0) // Handle error by defaulting to 0 max lift
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
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val workoutLogsList = document.get("workoutlog") as? List<Map<String, Any>> ?: emptyList()

                val filteredLogs = workoutLogsList
                    .filter { it["workoutName"] == workoutName }
                    .sortedWith(compareByDescending<Map<String, Any>> { it["date"] as String }
                        .thenByDescending { it["timestamp"] as Long })

                workoutLogs.clear()
                workoutLogs.addAll(filteredLogs.map {
                    WorkoutLog(
                        workoutName = it["workoutName"] as String,
                        weight = (it["weight"] as? Number)?.toDouble() ?: 0.0,
                        reps = (it["reps"] as? Number)?.toInt() ?: 0,
                        xp = (it["xp"] as? Number)?.toInt() ?: 0,
                        timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                        date = it["date"] as String
                    )
                })

                val logDisplayList = workoutLogs.map { "${it.date} --- ${it.weight} lbs --- ${it.reps} reps" }
                logAdapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    logDisplayList
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(android.R.id.text1)
                        textView.setTextColor(Color.WHITE)
                        textView.typeface = ResourcesCompat.getFont(context, R.font.pixel_font)
                        return view
                    }
                }
                listViewLogs.adapter = logAdapter
                logAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load workout logs.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteWorkoutLog(log: WorkoutLog) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .update("workoutlog", FieldValue.arrayRemove(mapOf(
                "workoutName" to log.workoutName,
                "weight" to log.weight,
                "reps" to log.reps,
                "date" to log.date,
                "timestamp" to log.timestamp,
                "xp" to log.xp
            )))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Log deleted: -${log.xp} XP", Toast.LENGTH_SHORT).show()
                loadExistingLogs()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete log.", Toast.LENGTH_SHORT).show()
            }
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
