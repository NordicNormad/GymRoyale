package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.cs407.gymroyale.models.Challenge

class ChallengerFragment : Fragment() {

    private lateinit var challengesRecyclerView: RecyclerView
    private val challengesList = mutableListOf<Challenge>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenger, container, false)

        // Initialize RecyclerView
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        challengesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch challenges from Firestore
        fetchAvailableChallenges()

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
            .update("acceptedBy", userId, "status", "accepted")
            .addOnSuccessListener {
                Toast.makeText(context, "Challenge accepted!", Toast.LENGTH_SHORT).show()
                fetchAvailableChallenges()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error accepting challenge", e)
            }
    }


    private fun completeChallenge(challenge: Challenge) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val username = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"

        db.collection("challenges").document(challenge.id)
            .update(
                "completedBy", userId,
                "completedByUsername", username,
                "status", "completed"
            )
            .addOnSuccessListener {
                claimTrophies(challenge.trophiesReward) // Use the trophiesReward field
                Toast.makeText(context, "Challenge completed and trophies claimed!", Toast.LENGTH_SHORT).show()
                fetchAvailableChallenges()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error completing challenge", e)
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
