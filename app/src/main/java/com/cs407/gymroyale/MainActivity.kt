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
import com.cs407.gymroyalepackage.LandingPageFragment

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Log.d("MainActivity", "Current user: ${auth.currentUser?.uid}")

        // Ensure user is authenticated
        checkAuthentication()

        // Check for userId in intent for ProfileFragment navigation
        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            Log.d("MainActivity", "Navigating to ProfileFragment for user: $userId")
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit()
        } else if (savedInstanceState == null) {
            // Load the main content (LandingPageFragment)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .commit()
        }
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

}
