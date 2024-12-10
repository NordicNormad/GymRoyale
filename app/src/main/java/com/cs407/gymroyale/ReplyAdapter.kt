package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Reply

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

            // Open profile fragment when username is clicked
            usernameTextView.setOnClickListener {
                openProfileFragment(reply.userId)
            }
        }

        private fun openProfileFragment(userId: String) {
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId) // Pass the clicked user's ID
                }
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment) // Ensure fragment_container exists
                .addToBackStack(null)
                .commit()
        }

    }
}

