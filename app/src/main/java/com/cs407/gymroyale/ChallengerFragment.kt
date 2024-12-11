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
import android.widget.ImageView
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

        val gymIconImageView = view.findViewById<ImageView>(R.id.imageGymIcon)
        val bottomNavProfileButton = view.findViewById<Button>(R.id.buttonBottomNavProfile)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)
        // Initialize RecyclerView
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        challengesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch challenges from Firestore
        fetchAvailableChallenges()

        // Set the correct gym icon based on trophies
        updateGymIcon(gymIconImageView)

        // Handle Add Challenge Button
        val addChallengeButton = view.findViewById<Button>(R.id.addChallengeButton)
        addChallengeButton.setOnClickListener {
            showAddChallengeDialog()
        }

        bottomNavProfileButton.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO_PROFILE", true)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            requireActivity().finish()
        }

        bottomNavBountyButton.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO_BOUNTIES", true)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            requireActivity().finish()
        }

        bottomNavHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    private fun updateGymIcon(gymIconImageView: ImageView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch user's trophy count
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val trophies = document.getLong("trophies")?.toInt() ?: 0
                val gymIconRes = when (trophies) {
                    in 0..20 -> R.drawable.bronze
                    in 21..40 -> R.drawable.silver
                    in 41..60 -> R.drawable.gold
                    else -> R.drawable.legendary
                }
                gymIconImageView.setImageResource(gymIconRes)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching user's trophy count: ${e.message}", e)
            }
    }

    private fun getTrophyLevel(trophies: Int): String {
        return when {
            trophies <= 20 -> "Bronze"
            trophies in 21..40 -> "Silver"
            trophies in 41..60 -> "Gold"
            else -> "Legendary"
        }
    }

    private fun fetchAvailableChallenges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch user's trophy count
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val trophies = document.getLong("trophies")?.toInt() ?: 0
                val trophyLevel = getTrophyLevel(trophies)

                // Fetch challenges for the user's level
                db.collection("challenges")
                    .whereEqualTo("level", trophyLevel)
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
                        Log.w("Firestore", "Error fetching challenges: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching user's trophy count: ${e.message}", e)
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch user's trophy count
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userTrophies = document.getLong("trophies")?.toInt() ?: 0
                val level = getTrophyLevel(userTrophies)

                val challenge = hashMapOf(
                    "createdBy" to userId,
                    "completedBy" to emptyList<String>(),
                    "workout" to workout,
                    "weight" to weight,
                    "reps" to reps,
                    "date" to FieldValue.serverTimestamp(),
                    "trophies" to trophies,
                    "comments" to comments,
                    "level" to level // Add level to challenge
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
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching user's trophies for challenge creation: ${e.message}", e)
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

        challengeDetails.text = "Workout: ${challenge.workout}\nReps: ${challenge.reps}\nWeight: ${challenge.weight} lbs"

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
    private fun incrementUserTrophies(userId: String, trophies: Int) {
        db.collection("users").document(userId)
            .update("trophies", FieldValue.increment(trophies.toLong()))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Trophies updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating trophies: ${e.message}", e)
            }
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

                    // Add user to the challenge's completedBy array
                    db.collection("challenges").document(challenge.id)
                        .update("completedBy", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
                            incrementUserTrophies(userId, challenge.trophies)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Error updating challenge status.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error logging workout.", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    fun fetchMaxLogForChallenge(workoutName: String, userId: String, callback: (Double) -> Unit) {
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
                            Toast.makeText(requireContext(), "Challenge completed! $addXP XP added", Toast.LENGTH_SHORT).show()
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

    private fun openReplyPage(challenge: Challenge) {
        val intent = Intent(context, ReplyActivity::class.java)
        intent.putExtra("challengeId", challenge.id)
        startActivity(intent)
    }

}

