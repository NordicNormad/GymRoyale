package com.cs407.gymroyale.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.R
import com.cs407.gymroyale.models.Challenge

class ChallengesAdapter(
    private val challenges: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.bind(challenge)
        holder.itemView.setOnClickListener { onClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.challengeTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.challengeDescription)

        fun bind(challenge: Challenge) {
            titleTextView.text = challenge.title
            descriptionTextView.text = challenge.description
        }
    }
}
