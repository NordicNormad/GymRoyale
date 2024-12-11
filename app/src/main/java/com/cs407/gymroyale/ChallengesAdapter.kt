package com.cs407.gymroyale

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Challenge
import com.google.firebase.firestore.FirebaseFirestore

class ChallengesAdapter(
    private val challenges: List<Challenge>,
    private val onAcceptClick: (Challenge) -> Unit,
    private val onReplyClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()  // Initialize Firestore instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.bind(challenge)

        // Set listeners for buttons
        holder.acceptButton.setOnClickListener { onAcceptClick(challenge) }
        holder.replyButton.setOnClickListener { onReplyClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // UI elements for the workout, reps, weights, trophies, and comment
        private val workoutText: TextView = itemView.findViewById(R.id.tvWorkout)
        private val repsAndWeightText: TextView = itemView.findViewById(R.id.tvRepsAndWeight)
        private val trophiesText: TextView = itemView.findViewById(R.id.tvTrophies)
        private val commentsText: TextView = itemView.findViewById(R.id.tvComment)
        val acceptButton: Button = itemView.findViewById(R.id.btnAccept)
        val replyButton: Button = itemView.findViewById(R.id.btnReply)

        fun bind(challenge: Challenge) {
            // Bind workout name initially
            workoutText.text = challenge.workout

            // Fetch the username from Firestore asynchronously
            FirebaseFirestore.getInstance()
                .collection("users")  // Adjust the collection name if needed
                .document(challenge.createdBy)  // Get the user document by the createdBy ID
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("name") ?: "Unknown User"  // Get the name field
                    workoutText.text = "${challenge.workout} by $username"  // Update text with username
                }
                .addOnFailureListener { exception ->
                    // Handle failure (e.g., log error or set default value)
                    Log.e("ChallengesAdapter", "Error fetching username", exception)
                    workoutText.text = "${challenge.workout} by Unknown User"  // Default fallback
                }

            // Bind reps and weight
            repsAndWeightText.text = "Reps: ${challenge.reps}, Weight: ${challenge.weight} lbs"

            // Bind trophies
            trophiesText.text = "Trophies: ${challenge.trophies}"

            // Bind comments
            commentsText.text = challenge.comments
        }
    }
}
