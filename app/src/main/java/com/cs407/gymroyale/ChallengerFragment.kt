package com.cs407.lab7

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChallengesFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewChallenges)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch and display challenges
        firestore.collection("challenges")
            .get()
            .addOnSuccessListener { documents ->
                val challenges = documents.map { document ->
                    Pair(
                        document.id, // Challenge ID
                        document.data["title"] as String // Challenge Title
                    )
                }
                recyclerView.adapter = ChallengesAdapter(challenges) { challengeId, challengeTitle ->
                    onChallengeSelected(challengeId, challengeTitle)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching challenges: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onChallengeSelected(challengeId: String, challengeTitle: String) {
        // Navigate to ChallengerFoundActivity with challenge details
        val intent = Intent(requireContext(), ChallengerFoundActivity::class.java).apply {
            putExtra("challengeId", challengeId)
            putExtra("challengeTitle", challengeTitle)
        }
        startActivity(intent)
    }

}
