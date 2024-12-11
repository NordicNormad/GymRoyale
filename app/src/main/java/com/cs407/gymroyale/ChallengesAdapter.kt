package com.cs407.gymroyale

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Challenge

class ChallengesAdapter(
    private val challenges: List<Challenge>,
    private val onAcceptClick: (Challenge) -> Unit,
    private val onReplyClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

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
            // Bind data from the Challenge object to the UI
            workoutText.text = challenge.workout // Bind workout name
            repsAndWeightText.text = "Reps: ${challenge.reps}, Weight: ${challenge.weight} lbs" // Bind reps and weight
            trophiesText.text = "Trophies: ${challenge.trophies}" // Bind trophies
            commentsText.text = challenge.comments // Bind comments
        }
    }
}
