package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cs407.gymroyale.utils.FirebaseUtils

class LandingPageFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_landing_page, container, false)

        // Level and trophies elements
        val levelText = view.findViewById<TextView>(R.id.textLevel)
        val trophiesText = view.findViewById<TextView>(R.id.textTrophies)
        val gymIconImageView = view.findViewById<ImageView>(R.id.imageGymIcon)

        // Load UserInfo from Firestore
        FirebaseUtils.loadUserInfoFromFirestore { userInfo ->
            if (userInfo != null) {
                // Set level text with fraction
                val currentLevel = userInfo.Level.toInt()
                val progressDecimal = userInfo.Level - currentLevel
                val progressFraction = (progressDecimal * 10000).toInt()
                levelText.text = "Level: $currentLevel, $progressFraction/10000"

                // Set trophies text
                trophiesText.text = "Trophies: ${userInfo.Trophies}"

                // Set gym icon based on trophies range
                val gymIconRes = when (userInfo.Trophies) {
                    in 0..200 -> R.drawable.arena1icon
                    in 201..400 -> R.drawable.arena2icon
                    in 401..600 -> R.drawable.arena3icon
                    else -> R.drawable.arena4icon
                }
                gymIconImageView.setImageResource(gymIconRes)
            } else {
                // Handle missing user info (e.g., show default UI or an error message)
                levelText.text = "Level: Unknown"
                trophiesText.text = "Trophies: Unknown"
                gymIconImageView.setImageResource(R.drawable.arena1icon)
            }
        }

        // Button Definitions
        val profileButton = view.findViewById<Button>(R.id.buttonProfile)
        val logWorkoutButton = view.findViewById<Button>(R.id.buttonLogWorkout)
        val challengesButton = view.findViewById<Button>(R.id.buttonChallenges)

        // Profile button action
        profileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Load WorkoutStorageFragment when the Log Workout button is clicked
        logWorkoutButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchWorkout::class.java)
            startActivityForResult(intent, 1)
        }

        // Load ChallengesFragment when the Find Challenger button is clicked
        challengesButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChallengesFragment())
                .addToBackStack(null)
                .commit()
        }

        // Bottom navigation bar buttons
        val bottomNavSettingsButton = view.findViewById<Button>(R.id.buttonBottomNavSettings)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)

        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavSettingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsMain())
                .addToBackStack(null)
                .commit()
        }

        bottomNavHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
