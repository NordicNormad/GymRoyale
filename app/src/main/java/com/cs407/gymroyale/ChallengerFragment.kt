package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.cs407.gymroyale.models.Challenge
import com.cs407.gymroyalepackage.LandingPageFragment

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
                    { challenge -> completeChallenge(challenge) },
                    { challenge -> openReplyPage(challenge) }
                )
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching challenges", e)
            }
    }

    private fun acceptChallenge(challenge: Challenge) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("challenges").document(challenge.id)
            .update("status", "accepted", "acceptedBy", userId)
            .addOnSuccessListener {
                Toast.makeText(context, "Challenge accepted!", Toast.LENGTH_SHORT).show()
                fetchAvailableChallenges() // Refresh the list
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error accepting challenge", e)
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
                            claimTrophies(challenge.trophiesReward)
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
