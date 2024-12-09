package com.cs407.gymroyale

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Reply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReplyActivity : AppCompatActivity() {

    private lateinit var repliesRecyclerView: RecyclerView
    private lateinit var replyInput: EditText
    private lateinit var postReplyButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var challengeId: String
    private val repliesList = mutableListOf<Reply>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply)

        // Get challenge ID from intent
        challengeId = intent.getStringExtra("challengeId") ?: return

        // Initialize views
        repliesRecyclerView = findViewById(R.id.repliesRecyclerView)
        replyInput = findViewById(R.id.replyInput)
        postReplyButton = findViewById(R.id.postReplyButton)

        // Set up RecyclerView
        repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchReplies()

        // Post reply button action
        postReplyButton.setOnClickListener {
            postReply()
        }
    }

    private fun fetchReplies() {
        db.collection("challenges").document(challengeId).collection("replies")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ReplyActivity", "Error fetching replies", e)
                    return@addSnapshotListener
                }

                repliesList.clear()
                for (document in snapshots!!) {
                    val reply = document.toObject(Reply::class.java)
                    repliesList.add(reply)
                }

                repliesRecyclerView.adapter = ReplyAdapter(repliesList)
            }
    }

    private fun postReply() {
        val message = replyInput.text.toString().trim()
        if (TextUtils.isEmpty(message)) {
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val username = auth.currentUser?.displayName ?: "Anonymous"

        val reply = Reply(
            userId = userId,
            username = username,
            message = message,
            timestamp = java.util.Date()
        )

        db.collection("challenges").document(challengeId).collection("replies")
            .add(reply)
            .addOnSuccessListener {
                replyInput.text.clear()
                fetchReplies()
            }
            .addOnFailureListener { e ->
                Log.w("ReplyActivity", "Error posting reply", e)
            }
    }
}
