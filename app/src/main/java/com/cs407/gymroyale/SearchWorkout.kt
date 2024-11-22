package com.cs407.gymroyale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
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
        setContentView(R.layout.fragment_search_workout)

        searchView = findViewById(R.id.searchView)
        buttonSearch = findViewById(R.id.buttonSearch)

        // Load workout data from the CSV file
        lifecycleScope.launch {
            workoutList = readWorkoutsFromCsv()
            setupAutoComplete()
        }

        buttonSearch.setOnClickListener {
            val query = searchView.text.toString()
            selectWorkout(query)
        }
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
            // Return the selected workout to the calling fragment
            val intent = Intent()
            intent.putExtra(WorkoutStorageFragment.RESULT_WORKOUT, filteredWorkouts[0])
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
