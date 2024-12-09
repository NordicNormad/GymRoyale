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
    private lateinit var repliesAdapter: ReplyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply)

        // Get challenge ID from intent
        challengeId = intent.getStringExtra("challengeId") ?: run {
            Log.e("ReplyActivity", "Challenge ID is null, finishing activity.")
            finish()
            return
        }

        // Initialize views
        repliesRecyclerView = findViewById(R.id.repliesRecyclerView)
        replyInput = findViewById(R.id.replyInput)
        postReplyButton = findViewById(R.id.postReplyButton)

        // Set up RecyclerView
        repliesAdapter = ReplyAdapter(repliesList)
        repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        repliesRecyclerView.adapter = repliesAdapter

        // Fetch replies
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

                // Notify adapter of dataset changes
                repliesAdapter.notifyDataSetChanged()
            }
    }

    private fun postReply() {
        val message = replyInput.text.toString().trim()
        if (TextUtils.isEmpty(message)) {
            replyInput.error = "Message cannot be empty"
            return
        }

        val userId = auth.currentUser?.uid ?: run {
            Log.e("ReplyActivity", "User is not authenticated, cannot post reply.")
            return
        }
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
                Log.d("ReplyActivity", "Reply successfully posted")
            }
            .addOnFailureListener { e ->
                Log.w("ReplyActivity", "Error posting reply", e)
            }
    }
}
