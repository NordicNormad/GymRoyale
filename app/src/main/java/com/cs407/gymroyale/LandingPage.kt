package com.cs407.gymroyalepackage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cs407.gymroyale.BountyFragment
import com.cs407.gymroyale.ChallengerFragment
import com.cs407.gymroyale.ProfileFragment
import com.cs407.gymroyale.R
import com.cs407.gymroyale.SearchWorkout
import com.cs407.gymroyale.utils.FirebaseUtils

class LandingPageFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
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

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit, popEnter, popExit)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

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
            try {
                if (userInfo != null) {
                    // Set level text with fraction
                    val currentLevel = userInfo.xp
                    levelText.text = "Experience Lvl: $currentLevel"

                    // Handle trophies with a fallback to default value of 0
                    val trophies = userInfo.trophies ?: 0
                    trophiesText.text = "Trophies: $trophies"

                    // Set gym icon based on trophies range
                    val gymIconRes = when (trophies) {
                        in 0..20 -> R.drawable.bronze
                        in 21..40 -> R.drawable.silver
                        in 41..60 -> R.drawable.gold
                        else -> R.drawable.legendary
                    }
                    gymIconImageView.setImageResource(gymIconRes)
                } else {
                    // Handle missing user info
                    levelText.text = "Level: Unknown"
                    trophiesText.text = "Trophies: 0"
                    gymIconImageView.setImageResource(R.drawable.bronze)
                }
            } catch (e: Exception) {
                Log.e("LandingPageFragment", "Error loading user info: ${e.message}", e)
                levelText.text = "Level: Unknown"
                trophiesText.text = "Trophies: 0"
                gymIconImageView.setImageResource(R.drawable.bronze)
            }
        }

        // Button Definitions
        val logWorkoutButton = view.findViewById<Button>(R.id.buttonLogWorkout)
        val challengesButton = view.findViewById<Button>(R.id.buttonChallenges)

        // Load WorkoutStorageFragment when the Log Workout button is clicked
        logWorkoutButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchWorkout::class.java)
            startActivityForResult(intent, 1)
        }

        // Load ChallengesFragment when the Find Challenger button is clicked
        challengesButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChallengerFragment())
                .addToBackStack(null)
                .commit()
        }

        // Bottom navigation bar buttons
        val bottomNavProfileButton = view.findViewById<Button>(R.id.buttonBottomNavProfile)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)

        bottomNavBountyButton.setOnClickListener {
            navigateFragment(BountyFragment(), slideInFromRight = true)   // left to right
        }

        bottomNavProfileButton.setOnClickListener {
            navigateFragment(ProfileFragment(), slideInFromRight = false)  // right to left
        }
        return view
    }
}
