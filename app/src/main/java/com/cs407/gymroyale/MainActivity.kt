package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import com.cs407.gymroyale.models.Challenge
import com.cs407.gymroyalepackage.LandingPageFragment

class MainActivity : AppCompatActivity() {
    private fun navigateFragment(fragment: Fragment, slideInFromRight: Boolean) {
        val (enter, exit, popEnter, popExit) = if (slideInFromRight) {
            arrayOf(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else {
            arrayOf(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit, popEnter, popExit)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
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

        // Handle navigation flags first
        if (intent.getBooleanExtra("NAVIGATE_TO_PROFILE", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        } else if (intent.getBooleanExtra("NAVIGATE_TO_BOUNTIES", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .commit()
        } else if (intent.getStringExtra("userId") != null) {
            // Your existing userId handling
            val userId = intent.getStringExtra("userId")
            Log.d("MainActivity", "Navigating to ProfileFragment for user: $userId")
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit()
        } else if (savedInstanceState == null) {
            // Only load LandingPageFragment if no other navigation is specified
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
