package com.cs407.lab7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.R

class ChallengesAdapter(
    private val challenges: List<Pair<String, String>>, // Pair of Challenge ID and Title
    private val onChallengeClick: (String, String) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textChallengeTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val (challengeId, challengeTitle) = challenges[position]
        holder.title.text = challengeTitle
        holder.itemView.setOnClickListener {
            onChallengeClick(challengeId, challengeTitle)
        }
    }

    override fun getItemCount() = challenges.size
}
