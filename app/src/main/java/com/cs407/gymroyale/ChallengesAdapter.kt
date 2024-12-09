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
    private val onCompleteClick: (Challenge) -> Unit,
    private val onReplyClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.bind(challenge)
        holder.itemView.setOnClickListener { onCompleteClick(challenge) }
        holder.replyButton.setOnClickListener { onReplyClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.challengeTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.challengeDescription)
        private val uploadedByText: TextView = itemView.findViewById(R.id.uploadedBy)
        private val completedByText: TextView = itemView.findViewById(R.id.completedBy)
        val replyButton: Button = itemView.findViewById(R.id.replyButton)

        fun bind(challenge: Challenge) {
            titleText.text = challenge.title
            descriptionText.text = challenge.description

            // Show username of the uploader
            uploadedByText.text = "Uploaded by: ${challenge.uploadedByUsername ?: "Unknown"}"

            // Show username of the person who completed the challenge
            completedByText.text = if (challenge.completedByUsername != null) {
                "Completed by: ${challenge.completedByUsername}"
            } else {
                "Completed by: Not completed yet"
            }
        }
    }
}
