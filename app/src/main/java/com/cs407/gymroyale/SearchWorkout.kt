package com.cs407.gymroyale

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
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
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var workoutList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search_workout)  // Use the correct layout

        searchView = findViewById(R.id.searchView)
        buttonSearch = findViewById(R.id.buttonSearch)

        // Load workout data from the CSV file
        lifecycleScope.launch {
            workoutList = readWorkoutsFromCsv()
            setupAutoComplete()
        }

        // Search button functionality
        buttonSearch.setOnClickListener {
            val query = searchView.text.toString()
            searchWorkouts(query)
        }
    }

    // Function to read workouts from the CSV file in res/raw
    private suspend fun readWorkoutsFromCsv(): List<String> {
        return withContext(Dispatchers.IO) {
            val workouts = mutableListOf<String>()
            val inputStream = resources.openRawResource(R.raw.workouts)  // Assuming your CSV is called workouts.csv
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    workouts.add(line.trim())  // Add each workout name to the list
                }
            }

            workouts
        }
    }

    // Setup the AutoCompleteTextView with the workout names
    private fun setupAutoComplete() {
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            workoutList
        )
        searchView.setAdapter(adapter)
    }

    // Search for a workout and display a Toast
    private fun searchWorkouts(query: String) {
        val filteredWorkouts = workoutList.filter { it.contains(query, ignoreCase = true) }

        if (filteredWorkouts.isNotEmpty()) {
            Toast.makeText(applicationContext, "Found: ${filteredWorkouts[0]}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "No workouts found", Toast.LENGTH_SHORT).show()
        }
    }
}
