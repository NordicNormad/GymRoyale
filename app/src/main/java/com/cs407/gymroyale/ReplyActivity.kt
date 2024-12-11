package com.cs407.gymroyale

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyale.models.Reply
import com.cs407.gymroyale.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class ReplyActivity : AppCompatActivity() {

    private lateinit var repliesRecyclerView: RecyclerView
    private lateinit var replyInput: EditText
    private lateinit var postReplyButton: Button
    private lateinit var buttonCancel: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var challengeId: String
    private val repliesList = mutableListOf<Reply>()

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
        buttonCancel = findViewById(R.id.buttonCancel)

        // Set up RecyclerView
        repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        repliesRecyclerView.adapter = ReplyAdapter(repliesList, supportFragmentManager)

        // Fetch replies
        fetchReplies()

        // Post reply button action
        postReplyButton.setOnClickListener {
            postReply()
        }
        buttonCancel.setOnClickListener {
            finish()
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

                repliesRecyclerView.adapter?.notifyDataSetChanged()
            }
    }

    private fun postReply() {
        val message = replyInput.text.toString().trim()
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Fetch user details to post reply
        FirebaseUtils.fetchUserProfile(userId) { userInfo ->
            if (userInfo != null) {
                val reply = Reply(
                    userId = userId,
                    name = userInfo.name,
                    profileLink = userInfo.bio, // Optional if needed
                    message = message,
                    timestamp = Date()
                )

                // Add the reply to Firestore
                db.collection("challenges").document(challengeId).collection("replies")
                    .add(reply)
                    .addOnSuccessListener {
                        replyInput.text.clear()
                        fetchReplies()
                        Toast.makeText(this, "Reply posted!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("ReplyActivity", "Error posting reply", e)
                    }
            } else {
                Toast.makeText(this, "Failed to fetch user profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showUserProfile(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Name not set"
                    val userStats = "XP: ${document.getLong("xp") ?: 0}, Trophies: ${document.getLong("trophies") ?: 0}"

                    // Open the dialog with fetched data
                    val dialog = UserProfileDialog.newInstance(userName, userStats)
                    dialog.show(supportFragmentManager, "UserProfileDialog")
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("ReplyActivity", "Error fetching profile", e)
            }
    }
}
