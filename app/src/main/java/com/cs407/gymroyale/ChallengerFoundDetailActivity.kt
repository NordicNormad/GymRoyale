package com.cs407.gymroyale

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.gymroyale.R
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

        // Display the challenge title
        textChallengerFound.text = "Challenge: $challengeTitle"

        // Fetch and display opponent details (mocked for now)
        fetchOpponentDetails(challengeId) { opponentName, rank ->
            textOpponent.text = "Opponent: $opponentName"
            textRank.text = "Rank: $rank"
        }

        // Handle cancel button
        buttonCancel.setOnClickListener {
            Toast.makeText(this, "Challenger screen canceled!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchOpponentDetails(challengeId: String?, callback: (String, String) -> Unit) {
        if (challengeId == null) {
            callback("No Opponent", "N/A")
            return
        }

        // Simulate fetching opponent data
        FirebaseFirestore.getInstance().collection("challenges").document(challengeId)
            .collection("participants").limit(1).get()
            .addOnSuccessListener { documents ->
                val opponentName = documents.documents.firstOrNull()?.getString("name") ?: "Opponent Not Found"
                val rank = documents.documents.firstOrNull()?.getLong("rank")?.toString() ?: "N/A"
                callback(opponentName, rank)
            }
            .addOnFailureListener {
                callback("Error Fetching Opponent", "N/A")
            }
    }

}