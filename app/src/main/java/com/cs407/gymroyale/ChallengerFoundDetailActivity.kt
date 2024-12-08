package com.cs407.lab7

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChallengerFoundDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenger_found_display)

        // Get challenge details from the intent
        val challengeId = intent.getStringExtra("challengeId")
        val challengeTitle = intent.getStringExtra("challengeTitle")

        val textChallengerFound: TextView = findViewById(R.id.textChallengerFound)
        val textOpponent: TextView = findViewById(R.id.textOpponent)
        val textRank: TextView = findViewById(R.id.textRank)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewLinkedProfiles)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Display the challenge title
        textChallengerFound.text = "Challenge: $challengeTitle"

        // Fetch and display opponent details
        fetchOpponentDetails(challengeId) { opponentName, rank ->
            textOpponent.text = "Opponent: $opponentName"
            textRank.text = "Rank: $rank"
        }

        // Handle cancel button
        buttonCancel.setOnClickListener {
            Toast.makeText(this, "Challenger screen canceled!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Fetch linked profiles and set adapter
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        fetchLinkedProfiles(userId) { profiles ->
            recyclerView.adapter = LinkedProfilesAdapter(profiles)
        }
    }

    /**
     * Fetches opponent details for the given challenge and calls the callback with the result.
     */
    private fun fetchOpponentDetails(challengeId: String?, callback: (String, String) -> Unit) {
        if (challengeId == null) {
            callback("No Opponent", "N/A")
            return
        }

        FirebaseFirestore.getInstance().collection("challenges").document(challengeId)
            .collection("participants").limit(1).get()
            .addOnSuccessListener { documents ->
                val opponentName = documents.documents.firstOrNull()?.getString("name") ?: "Opponent Not Found"
                val rank = documents.documents.firstOrNull()?.getLong("rank")?.toString() ?: "N/A"

                // Optionally link profiles if required
                val opponentId = documents.documents.firstOrNull()?.id
                val challengerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                if (opponentId != null) {
                    linkProfiles(challengerId, opponentId, challengeId)
                }

                callback(opponentName, rank)
            }
            .addOnFailureListener { exception ->
                Log.e("OpponentDetails", "Error fetching opponent details: ${exception.message}")
                callback("Error Fetching Opponent", "N/A")
            }
    }

    /**
     * Fetches linked profiles for the given user and calls the callback with the result.
     */
    private fun fetchLinkedProfiles(userId: String, callback: (List<Pair<String, String>>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).collection("linkedProfiles").get()
            .addOnSuccessListener { documents ->
                val profiles = documents.map { document ->
                    Pair(
                        document.getString("name") ?: "Unknown",
                        document.getString("challengeId") ?: "Unknown Challenge"
                    )
                }
                callback(profiles)
            }
            .addOnFailureListener { e ->
                Log.e("LinkedProfiles", "Error fetching linked profiles: ${e.message}")
                Toast.makeText(this, "Failed to fetch linked profiles.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Links two profiles by updating their respective linkedProfiles sub-collections in Firestore.
     */
    private fun linkProfiles(challengerId: String, opponentId: String, challengeId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // References for each user's linkedProfiles sub-collection
        val challengerRef = firestore.collection("users").document(challengerId)
        val opponentRef = firestore.collection("users").document(opponentId)

        // Data to add for each user's profile
        val challengerData = mapOf("name" to "Opponent", "challengeId" to challengeId)
        val opponentData = mapOf("name" to "Challenger", "challengeId" to challengeId)

        // Add the data in a batch to ensure consistency
        firestore.runBatch { batch ->
            batch.set(challengerRef.collection("linkedProfiles").document(opponentId), challengerData)
            batch.set(opponentRef.collection("linkedProfiles").document(challengerId), opponentData)
        }.addOnSuccessListener {
            Log.d("LinkProfiles", "Profiles linked successfully!")
        }.addOnFailureListener { e ->
            Log.e("LinkProfiles", "Failed to link profiles: ${e.message}")
        }
    }
}
