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

        val menuButton = view.findViewById<Button>(R.id.buttonMenu)
        val profileButton = view.findViewById<Button>(R.id.buttonProfile)
        val logWorkoutButton = view.findViewById<Button>(R.id.buttonLogWorkout)
        val findChallengerButton = view.findViewById<Button>(R.id.buttonFindChallenger)
        val bottomNavMenuButton = view.findViewById<Button>(R.id.buttonBottomNavMenu)
        val bottomNavWorkoutButton = view.findViewById<Button>(R.id.buttonBottomNavWorkout)
        val bottomNavHistoryButton = view.findViewById<Button>(R.id.buttonBottomNavHistory)

        menuButton.setOnClickListener { /* Empty */ }
        profileButton.setOnClickListener { /* Empty */ }
        logWorkoutButton.setOnClickListener { /* Empty */ }

        // Load LoadingScreenFragment when the Find Challenger button is clicked
        findChallengerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoadingScreenFragment())
                .addToBackStack(null)
                .commit()
        }


        bottomNavMenuButton.setOnClickListener { /* Empty */ }
        bottomNavWorkoutButton.setOnClickListener { /* Empty */ }
        bottomNavHistoryButton.setOnClickListener { /* Empty */ }

        return view
    }
}
