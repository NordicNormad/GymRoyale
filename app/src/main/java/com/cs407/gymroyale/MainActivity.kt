package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import com.cs407.gymroyale.models.Challenge

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Log.d("MainActivity", "Current user: ${auth.currentUser?.uid}")

        // Ensure user is authenticated
        checkAuthentication()

        // Load the main content (LandingPageFragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .commit()
        }

        // Set up the Profile button
        val profileButton = findViewById<Button>(R.id.buttonProfile)
        profileButton.setOnClickListener {
            handleProfileButtonClick()
        }

        // Listen for challenge matches in real-time
        listenForMatches()
    }

    private fun checkAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to LoginActivity if user is not logged in
            Log.d("MainActivity", "No user logged in. Redirecting to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("MainActivity", "User logged in: ${currentUser.email}")
        }
    }

    private fun handleProfileButtonClick() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to LoginActivity if user is not logged in
            Log.d("MainActivity", "No user logged in. Redirecting to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // Load the ProfileFragment if the user is logged in
            Log.d("MainActivity", "User logged in: ${currentUser.email}. Loading ProfileFragment.")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun listenForMatches() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Log.w("MainActivity", "User ID is null. Cannot listen for matches.")
            return
        }

        db.collection("challenges")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (docChange in snapshots!!.documentChanges) {
                    if (docChange.type == DocumentChange.Type.ADDED) {
                        val challenge = docChange.document.toObject(Challenge::class.java)
                        // Notify user about the challenge
                        Toast.makeText(
                            this,
                            "Matched to challenge: ${challenge.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
