package com.cs407.gymroyale

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Challenge

class ChallengesAdapter(
    private val challenges: List<Challenge>,
    private val onCompleteClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.bind(challenge)
        holder.itemView.setOnClickListener { onCompleteClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.challengeTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.challengeDescription)

        fun bind(challenge: Challenge) {
            titleText.text = challenge.title
            descriptionText.text = challenge.description
        }
    }
}
