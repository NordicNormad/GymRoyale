package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.File

class WorkoutStorageFragment : Fragment() {

    private lateinit var workoutTypeInput: EditText
    private lateinit var workoutDetailsInput: EditText
    private lateinit var saveButton: Button
    private lateinit var searchButton: Button


    companion object {
        const val REQUEST_SEARCH = 1
        const val RESULT_WORKOUT = "selected_workout"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_storage, container, false)
        val bottomNavSettingsButton = view.findViewById<Button>(R.id.buttonBottomNavSettings)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)

        workoutTypeInput = view.findViewById(R.id.editWorkoutType)
        workoutDetailsInput = view.findViewById(R.id.editWorkoutDetails)
        saveButton = view.findViewById(R.id.btnSaveWorkout)
        searchButton = view.findViewById(R.id.btnSearchWorkout)

        saveButton.setOnClickListener {
            saveWorkoutData()
        }

        searchButton.setOnClickListener {
            openSearchWorkoutFragment()
        }

        // Load BountyFragment when the Bounties button is clicked
        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavSettingsButton.setOnClickListener { /* Empty */ }
        bottomNavHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun saveWorkoutData() {
        val workoutType = workoutTypeInput.text.toString().trim()
        val workoutDetails = workoutDetailsInput.text.toString().trim()

        // Validate input
        if (workoutType.isEmpty() || workoutType.length != 5 || !workoutType.matches(Regex("[A-Fa-f0-9]{5}"))) {
            Toast.makeText(requireContext(), "Workout type must be a 5-character hexadecimal code", Toast.LENGTH_SHORT).show()
            return
        }

        if (workoutDetails.isEmpty()) {
            Toast.makeText(requireContext(), "Workout details cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Save workout details to internal storage
        val csvContent = "WorkoutType,Muscle,RepsSet1,WeightSet1,RepsSet2,WeightSet2,...\n$workoutType,$workoutDetails"
        try {
            val fileName = "workout_data.csv"
            val file = File(requireContext().filesDir, fileName)
            file.writeText(csvContent)

            Toast.makeText(requireContext(), "Workout data saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving workout data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSearchWorkoutFragment() {
        val intent = Intent(requireContext(), SearchWorkout::class.java)
        startActivityForResult(intent, REQUEST_SEARCH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SEARCH && resultCode == AppCompatActivity.RESULT_OK) {
            val selectedWorkout = data?.getStringExtra(RESULT_WORKOUT)
            if (selectedWorkout != null) {
                workoutDetailsInput.setText(selectedWorkout)
            }
        }
    }
}
