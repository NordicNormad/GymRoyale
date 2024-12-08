package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class LandingPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_landing_page, container, false)

        // Button Definitions
        val profileButton = view.findViewById<Button>(R.id.buttonProfile)
        val logWorkoutButton = view.findViewById<Button>(R.id.buttonLogWorkout)
        val findChallengerButton = view.findViewById<Button>(R.id.buttonFindChallenger)

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

        // Load LoadingScreenFragment when the Find Challenger button is clicked
        findChallengerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoadingScreenFragment())
                .addToBackStack(null)
                .commit()
        }

////////////////////////////////////////////////////////BOTTOM BAR//////////////////////////
        // Button Definitions
        val bottomNavSettingsButton = view.findViewById<Button>(R.id.buttonBottomNavSettings)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)

        // Load BountyFragment when the Bounties button is clicked
        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        // Settings button open
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
        ////////////////////////////////////////////////////////////////////////////////////////////

        return view
    }
}
