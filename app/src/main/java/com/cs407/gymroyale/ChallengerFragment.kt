package com.cs407.gymroyale

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.cs407.gymroyale.models.Challenge

class ChallengesFragment : Fragment() {

    private lateinit var challengesRecyclerView: RecyclerView
    private val challengesList = mutableListOf<Challenge>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenges, container, false)

        // RecyclerView setup
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        challengesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load challenges
        fetchAvailableChallenges()

        // Add challenge button
        val addChallengeButton = view.findViewById<View>(R.id.buttonAddChallenge)
        addChallengeButton.setOnClickListener {
            showAddChallengeDialog()
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
                // Update RecyclerView adapter
                challengesRecyclerView.adapter = ChallengesAdapter(challengesList) { challenge ->
                    completeChallenge(challenge)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching challenges", e)
            }
    }

    private fun showAddChallengeDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Add New Challenge")

        // Create input fields
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val titleInput = EditText(requireContext()).apply {
            hint = "Challenge Title"
        }
        val descriptionInput = EditText(requireContext()).apply {
            hint = "Challenge Description"
        }

        layout.addView(titleInput)
        layout.addView(descriptionInput)

        dialogBuilder.setView(layout)

        // Add action buttons
        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val title = titleInput.text.toString()
            val description = descriptionInput.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                uploadChallenge(title, description)
            } else {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.show()
    }

    private fun uploadChallenge(title: String, description: String) {
        val challenge = hashMapOf(
            "title" to title,
            "description" to description,
            "createdBy" to FirebaseAuth.getInstance().currentUser?.uid,
            "participants" to emptyList<String>(),
            "status" to "open"
        )

        db.collection("challenges")
            .add(challenge)
            .addOnSuccessListener {
                Toast.makeText(context, "Challenge added successfully", Toast.LENGTH_SHORT).show()
                fetchAvailableChallenges()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding challenge", e)
            }
    }

    private fun completeChallenge(challenge: Challenge) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("challenges").document(challenge.id)
            .update("status", "completed")
            .addOnSuccessListener {
                // Award XP (example logic)
                val userRef = db.collection("users").document(userId)
                userRef.update("xp", FieldValue.increment(50)) // Increment XP by 50
                    .addOnSuccessListener {
                        Toast.makeText(context, "Challenge completed! XP gained!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating XP", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error completing challenge", e)
            }
    }
}
