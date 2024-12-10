package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SearchWorkout : AppCompatActivity() {

    private lateinit var searchView: AutoCompleteTextView
    private lateinit var buttonSearch: Button
    private lateinit var buttonViewToday: Button
    private lateinit var buttonCancel: Button
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var workoutList: List<String>
    private lateinit var csvManager: WorkoutLogCSVManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_workout)

        csvManager = WorkoutLogCSVManager(this)

        searchView = findViewById(R.id.searchView)
        buttonSearch = findViewById(R.id.buttonSearch)
        buttonViewToday = findViewById(R.id.buttonViewToday)
        buttonCancel = findViewById(R.id.buttonCancel)

        // Load workout data from the CSV file
//        lifecycleScope.launch {
//            workoutList = readWorkoutsFromCsv()
//            setupAutoComplete()
//        }

        val preSelectedWorkout = intent.getStringExtra("SELECTED_WORKOUT")

        // Load workout data from the CSV file
        lifecycleScope.launch {
            workoutList = readWorkoutsFromCsv()
            setupAutoComplete()

            // If a workout was pre-selected, automatically select it
            preSelectedWorkout?.let { workout ->
                searchView.setText(workout)
                selectWorkout(workout)
            }
        }


        buttonSearch.setOnClickListener {
            val query = searchView.text.toString()
            selectWorkout(query)
        }

        buttonViewToday.setOnClickListener {
            showTodayWorkouts()
        }

        // Handle Cancel button click to finish the activity
        buttonCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)  // Assuming MainActivity hosts the fragments
            intent.putExtra("NAVIGATE_TO_HOME", true)  // Use an extra flag to signal home navigation
            startActivity(intent)
            finish()  // Close the current activity
        }



        // Inside onCreate method
        val historyButton = findViewById<Button>(R.id.historyButton)
        historyButton.setOnClickListener {
            val intent = Intent(this, WorkoutHistoryActivity::class.java)
            startActivity(intent)
        }

    }

    private fun showTodayWorkouts() {
        val todayFragment = TodayWorkoutsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, todayFragment)
            .addToBackStack(null)
            .commit()
    }

    private suspend fun readWorkoutsFromCsv(): List<String> {
        return withContext(Dispatchers.IO) {
            val workouts = mutableListOf<String>()
            val inputStream = resources.openRawResource(R.raw.workouts)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    workouts.add(line.trim())
                }
            }

            workouts
        }
    }

    private fun setupAutoComplete() {
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            workoutList
        )
        searchView.setAdapter(adapter)
    }

    private fun selectWorkout(query: String) {
        val filteredWorkouts = workoutList.filter { it.contains(query, ignoreCase = true) }

        if (filteredWorkouts.isNotEmpty()) {
            val workoutName = filteredWorkouts[0]

            // Navigate to WorkoutLogFragment
            val fragment = WorkoutLogFragment.newInstance(workoutName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            Toast.makeText(applicationContext, "No workouts found", Toast.LENGTH_SHORT).show()
        }
    }
}
