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
        holder.acceptButton.setOnClickListener { onAcceptClick(challenge) }
        holder.completeButton.setOnClickListener { onCompleteClick(challenge) }
        holder.replyButton.setOnClickListener { onReplyClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.challengeTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.challengeDescription)
        private val trophiesText: TextView = itemView.findViewById(R.id.challengeTrophiesReward)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val completeButton: Button = itemView.findViewById(R.id.completeButton)
        val replyButton: Button = itemView.findViewById(R.id.replyButton)

        fun bind(challenge: Challenge) {
            titleText.text = challenge.title
            descriptionText.text = challenge.description
            trophiesText.text = "Trophies: ${challenge.trophiesReward}"
        }
    }
}
