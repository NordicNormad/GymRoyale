package com.cs407.lab7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.R

class LinkedProfilesAdapter(
    private val profiles: List<Pair<String, String>> // Pair of name and challengeId
) : RecyclerView.Adapter<LinkedProfilesAdapter.ProfileViewHolder>() {

    // ViewHolder class to hold references to the views
    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textLinkedProfileName)
        val challengeTextView: TextView = itemView.findViewById(R.id.textLinkedChallengeId)
    }

    // Inflates the item layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_linked_profile, parent, false)
        return ProfileViewHolder(view)
    }

    // Binds the data to the views in each ViewHolder
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val (name, challengeId) = profiles[position]
        holder.nameTextView.text = "Name: $name"
        holder.challengeTextView.text = "Challenge: $challengeId"
    }

    // Returns the total number of items in the dataset
    override fun getItemCount() = profiles.size
}
