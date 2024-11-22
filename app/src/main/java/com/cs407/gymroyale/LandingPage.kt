package com.cs407.gymroyale

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

        val shopButton = view.findViewById<Button>(R.id.buttonShop)
        val profileButton = view.findViewById<Button>(R.id.buttonProfile)
        val logWorkoutButton = view.findViewById<Button>(R.id.buttonLogWorkout)
        val findChallengerButton = view.findViewById<Button>(R.id.buttonFindChallenger)
        val bottomNavSettingsButton = view.findViewById<Button>(R.id.buttonBottomNavSettings)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)

        shopButton.setOnClickListener { /* Empty */ }
        profileButton.setOnClickListener { /* Empty */ }
        logWorkoutButton.setOnClickListener { /* Empty */ }

        // Load LoadingScreenFragment when the Find Challenger button is clicked
        findChallengerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoadingScreenFragment())
                .addToBackStack(null)
                .commit()
        }

        // Load BountyFragment when the Bounties button is clicked
        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavSettingsButton.setOnClickListener { /* Empty */ }
        bottomNavHomeButton.setOnClickListener { /* Empty */ }

        return view
    }
}