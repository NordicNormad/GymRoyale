package com.cs407.gymroyale

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Reply
import com.google.firebase.firestore.FirebaseFirestore

class ReplyAdapter(
    private val replies: List<Reply>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(replies[position])
    }

    override fun getItemCount(): Int = replies.size

    inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(reply: Reply) {
            usernameTextView.text = reply.name
            messageTextView.text = reply.message
            timestampTextView.text = reply.timestamp.toString()

            usernameTextView.setOnClickListener {
                // Fetch user profile and show dialog
                fetchUserProfile(reply.userId)
            }
        }

        private fun fetchUserProfile(userId: String) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "Name not set"
                        val userStats = "XP: ${document.getLong("xp") ?: 0}, Trophies: ${document.getLong("trophies") ?: 0}"
                        val userBio = document.getString("bio") ?: "Bio not set"
                        // Open the UserProfileDialog with fetched data
                        val dialog = UserProfileDialog.newInstance(userName, userStats, userBio)
                        dialog.show(fragmentManager, "UserProfileDialog")
                    } else {
                        Toast.makeText(
                            itemView.context,
                            "User data not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        itemView.context,
                        "Error fetching profile: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReplyAdapter", "Error fetching profile", e)
                }
        }
    }
}
