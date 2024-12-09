package com.cs407.gymroyale

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Reply
import java.text.SimpleDateFormat
import java.util.Locale

class ReplyAdapter(private val replies: List<Reply>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replies[position]
        holder.bind(reply)
    }

    override fun getItemCount(): Int = replies.size

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.replyUsername)
        private val messageText: TextView = itemView.findViewById(R.id.replyMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.replyTimestamp)

        fun bind(reply: Reply) {
            usernameText.text = reply.username
            messageText.text = reply.message
            val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
            timestampText.text = dateFormat.format(reply.timestamp)
        }
    }
}
