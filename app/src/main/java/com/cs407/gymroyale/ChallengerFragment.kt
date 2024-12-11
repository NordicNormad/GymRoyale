package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.cs407.gymroyale.models.Challenge
import com.cs407.gymroyalepackage.LandingPageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChallengerFragment : Fragment() {

    private lateinit var challengesRecyclerView: RecyclerView
    private val challengesList = mutableListOf<Challenge>()
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenger, container, false)

        val bottomNavProfileButton = view.findViewById<Button>(R.id.buttonBottomNavProfile)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)
        // Initialize RecyclerView
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        challengesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch challenges from Firestore
        fetchAvailableChallenges()

        // Handle Add Challenge Button
        val addChallengeButton = view.findViewById<Button>(R.id.addChallengeButton)
        addChallengeButton.setOnClickListener {
            showAddChallengeDialog()
        }

        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavProfileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    private fun fetchAvailableChallenges() {
        db.collection("challenges")
            .get()
            .addOnSuccessListener { documents ->
                challengesList.clear()
                for (document in documents) {
                    val challenge = document.toObject(Challenge::class.java)
                    challenge.id = document.id
                    challengesList.add(challenge)
                }

                challengesRecyclerView.adapter = ChallengesAdapter(
                    challengesList,
                    { challenge -> acceptChallenge(challenge) },
                    { challenge -> openReplyPage(challenge) }
                )
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching challenges", e)
            }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddChallengeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_challenge, null)

        val workoutSpinner = dialogView.findViewById<Spinner>(R.id.workoutSpinner)
        val repsInput = dialogView.findViewById<EditText>(R.id.challengeRepsInput)
        val weightInput = dialogView.findViewById<EditText>(R.id.challengeWeightInput)
        val trophiesInput = dialogView.findViewById<EditText>(R.id.challengeTrophiesInput)
        val commentsInput = dialogView.findViewById<EditText>(R.id.challengeCommentsInput) // New Comments input

        // Load workouts from CSV and populate the spinner
        val workouts = loadWorkoutsFromCSV()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, workouts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workoutSpinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Add Challenge")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val selectedWorkout = workoutSpinner.selectedItem.toString()
                val reps = repsInput.text.toString().toIntOrNull() ?: 0
                val weight = weightInput.text.toString().toIntOrNull() ?: 0
                val trophies = trophiesInput.text.toString().toIntOrNull() ?: 0
                val comments = commentsInput.text.toString()  // Capture comments

                if (reps > 0 && weight > 0) {
                    addChallengeToFirestore(selectedWorkout, reps, weight, trophies, comments)
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun loadWorkoutsFromCSV(): List<String> {
        val workouts = mutableListOf<String>()
        val inputStream = resources.openRawResource(R.raw.workouts)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.forEach { line ->
                workouts.add(line.trim())
            }
        }
        return workouts
    }


    private fun addChallengeToFirestore(workout: String, weight: Int, reps: Int, trophies: Int, comments: String) {
        val challenge = hashMapOf(
            "createdBy" to FirebaseAuth.getInstance().currentUser?.uid,
            "completedBy" to emptyList<String>(),
            "workout" to workout,
            "weight" to weight,
            "reps" to reps,
            "date" to FieldValue.serverTimestamp(),
            "trophies" to trophies,
            "comments" to comments // Add comments
        )
        db.collection("challenges")
            .add(challenge)
            .addOnSuccessListener {
                Log.d("Firestore", "Challenge added successfully: ${it.id}")
                Toast.makeText(context, "Challenge added successfully", Toast.LENGTH_SHORT).show()
                fetchAvailableChallenges() // Refresh the list
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding challenge", e)
            }
    }


    private fun acceptChallenge(challenge: Challenge) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Check if user has already completed this challenge
        db.collection("challenges").document(challenge.id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val completedBy = document.get("completedBy") as? List<*>
                    if (completedBy != null && completedBy.contains(userId)) {
                        Toast.makeText(requireContext(), "Already completed this challenge.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Show confirmation dialog
                    showChallengeConfirmationDialog(challenge, userId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking challenge status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChallengeConfirmationDialog(challenge: Challenge, userId: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_challenge, null)
        val challengeDetails = dialogView.findViewById<TextView>(R.id.challengeDetails)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmLog)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancelLog)

        challengeDetails.text = "Workout: ${challenge.workout}\nReps: ${challenge.reps}\nWeight: ${challenge.weight} kg"

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Challenge")
            .setView(dialogView)

        val dialog = builder.create()

        confirmButton.setOnClickListener {
            // Log the workout to Firebase
            logWorkoutToFirebase(challenge, userId)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logWorkoutToFirebase(challenge: Challenge, userId: String) {
        // Fetch the max log for this specific workout
        fetchMaxLogForChallenge(challenge.workout, userId) { maxLiftSpecific ->
            val newLift = challenge.weight / ((100 - (challenge.reps * 2.5)) / 100)
            val expMultiplier = 1
            val expPr = maxOf((newLift - maxLiftSpecific + 100) * expMultiplier, 0.0).toInt()

            // Construct the workout log
            val workoutLog = mapOf(
                "workoutName" to challenge.workout,
                "weight" to challenge.weight,
                "reps" to challenge.reps,
                "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                "timestamp" to System.currentTimeMillis(),
                "xp" to expPr
            )

            // Add workout log to the user's workoutlog field
            db.collection("users").document(userId)
                .update("workoutlog", FieldValue.arrayUnion(workoutLog))
                .addOnSuccessListener {
                    // Increment user XP
                    incrementUserXPForChallenge(userId, expPr)

                    Toast.makeText(requireContext(), "Workout logged successfully!", Toast.LENGTH_SHORT).show()

                    // Add user to the challenge's completedBy array
                    db.collection("challenges").document(challenge.id)
                        .update("completedBy", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Challenge completed!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error updating challenge status.", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error logging workout.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchMaxLogForChallenge(workoutName: String, userId: String, callback: (Double) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val workoutLogs = document.get("workoutlog") as? List<Map<String, Any>> ?: emptyList()
                val maxLog = workoutLogs
                    .filter { it["workoutName"] == workoutName }
                    .maxByOrNull { log ->
                        val weight = when (val w = log["weight"]) {
                            is Double -> w
                            is Long -> w.toDouble()
                            else -> 0.0
                        }
                        val reps = when (val r = log["reps"]) {
                            is Long -> r.toInt()
                            is Int -> r
                            else -> 0
                        }
                        weight / ((100 - (reps * 2.5)) / 100)
                    }

                val maxLiftSpecific = maxLog?.let { log ->
                    val weight = when (val w = log["weight"]) {
                        is Double -> w
                        is Long -> w.toDouble()
                        else -> 0.0
                    }
                    val reps = when (val r = log["reps"]) {
                        is Long -> r.toInt()
                        is Int -> r
                        else -> 0
                    }
                    weight / ((100 - (reps * 2.5)) / 100)
                } ?: 0.0

                callback(maxLiftSpecific)
            }
            .addOnFailureListener {
                callback(0.0) // Handle error, default to 0 max lift
            }
    }

    private fun incrementUserXPForChallenge(userId: String, addXP: Int) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    var currentXP = document.getLong("xp")?.toInt() ?: 0
                    currentXP += addXP

                    // Update XP in Firestore
                    db.collection("users").document(userId)
                        .update("xp", currentXP)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "$addXP XP added", Toast.LENGTH_SHORT).show()
                            Log.d("ChallengerFragment", "XP successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("ChallengerFragment", "Error updating XP", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("ChallengerFragment", "Error retrieving user data", e)
            }
    }


    private fun completeChallenge(challenge: Challenge) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val challengeRef = db.collection("challenges").document(challenge.id)

        challengeRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val completedByField = document.get("completedBy")

                    // Convert completedBy to a list if it is not already
                    val completedBy = when (completedByField) {
                        is List<*> -> completedByField.filterIsInstance<String>()
                        is String -> listOf(completedByField)
                        else -> emptyList()
                    }

                    if (completedBy.contains(userId)) {
                        Toast.makeText(context, "You have already completed this challenge.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    challengeRef.update("completedBy", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
//                            claimTrophies(challenge.trophiesReward)
                            Toast.makeText(context, "Challenge completed and trophies claimed!", Toast.LENGTH_SHORT).show()
                            fetchAvailableChallenges() // Refresh the list
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error updating completedBy: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching challenge: $e")
            }
    }




    private fun claimTrophies(trophies: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("trophies", FieldValue.increment(trophies.toLong()))
            .addOnSuccessListener {
                Log.d("Firestore", "Trophies updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating trophies", e)
            }
    }

    private fun openReplyPage(challenge: Challenge) {
        val intent = Intent(context, ReplyActivity::class.java)
        intent.putExtra("challengeId", challenge.id)
        startActivity(intent)
    }
}
