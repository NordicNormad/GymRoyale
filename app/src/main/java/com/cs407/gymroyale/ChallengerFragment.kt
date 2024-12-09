package com.cs407.gymroyale

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.cs407.gymroyale.adapters.ChallengesAdapter
import com.cs407.gymroyale.models.Challenge
import com.google.firebase.firestore.FieldValue

class ChallengerFragment : Fragment() {

    private lateinit var challengesRecyclerView: RecyclerView
    private lateinit var challengesAdapter: ChallengesAdapter
    private val challengesList = mutableListOf<Challenge>()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_challenger, container, false)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set up RecyclerView
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        challengesAdapter = ChallengesAdapter(challengesList) { challenge ->
            joinChallenge(challenge.id)
        }
        challengesRecyclerView.layoutManager = LinearLayoutManager(context)
        challengesRecyclerView.adapter = challengesAdapter

        // Fetch Challenges
        fetchAvailableChallenges()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    private fun fetchAvailableChallenges() {
        listenerRegistration = db.collection("challenges")
            .whereEqualTo("status", "open")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching challenges", e)
                    return@addSnapshotListener
                }
                challengesList.clear()
                for (document in snapshots!!) {
                    val challenge = document.toObject(Challenge::class.java)
                    challenge.id = document.id // Assign document ID
                    challengesList.add(challenge)
                }
                challengesAdapter.notifyDataSetChanged()
            }
    }

    private fun joinChallenge(challengeId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("challenges").document(challengeId)
            .update("participants", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Toast.makeText(context, "Joined Challenge!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error joining challenge", e)
                Toast.makeText(context, "Failed to join challenge.", Toast.LENGTH_SHORT).show()
            }
    }
}
