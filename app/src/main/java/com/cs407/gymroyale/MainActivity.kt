package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase

        auth = FirebaseAuth.getInstance()
        Log.d("MainActivity", "Current user: ${auth.currentUser?.uid}")

        setContentView(R.layout.activity_main)

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
}