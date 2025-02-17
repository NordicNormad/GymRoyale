package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

class SearchWorkout : AppCompatActivity() {

    private lateinit var searchView: AutoCompleteTextView
    private lateinit var buttonSearch: Button
    private lateinit var buttonViewToday: Button
    private lateinit var buttonHome: Button
    private lateinit var buttonProfile: Button
    private lateinit var buttonBounties: Button
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var workoutList: List<String>

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
    override fun onResume() {
        super.onResume()
        setupKeyboardVisibilityListener()
    }

    private fun setupKeyboardVisibilityListener() {
        val rootLayout = findViewById<View>(android.R.id.content)
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val bottomBar = findViewById<LinearLayout>(R.id.bottomNavigation)
            val heightDiff = rootLayout.rootView.height - rootLayout.height
            if (heightDiff > 200) { // Keyboard is visible
                bottomBar.visibility = View.GONE
            } else {
                bottomBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_workout)

        searchView = findViewById(R.id.searchView)
        buttonSearch = findViewById(R.id.buttonSearch)
        buttonViewToday = findViewById(R.id.buttonViewToday)
        buttonHome = findViewById(R.id.buttonBottomNavHome)
        buttonProfile = findViewById(R.id.buttonBottomNavProfile)
        buttonBounties = findViewById(R.id.buttonBottomNavBounties)



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
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)  // Assuming MainActivity hosts the fragments
            intent.putExtra("NAVIGATE_TO_HOME", true)  // Use an extra flag to signal home navigation
            startActivity(intent)
            finish()  // Close the current activity
        }

        buttonProfile.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO_PROFILE", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        buttonBounties.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO_BOUNTIES", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
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
            .replace(R.id.fragment_container, todayFragment)
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
        searchView.setOnItemClickListener { _, _, _, _ ->
            val selectedWorkout = searchView.text.toString()
            hideKeyboard()
            selectWorkout(selectedWorkout)

        }
    }

    private fun selectWorkout(query: String) {
        val filteredWorkouts = workoutList.filter { it.contains(query, ignoreCase = true) }

        if (filteredWorkouts.isNotEmpty()) {
            val workoutName = filteredWorkouts[0]

            // Navigate to WorkoutLogFragment
            val fragment = WorkoutLogFragment.newInstance(workoutName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            Toast.makeText(applicationContext, "No workouts found", Toast.LENGTH_SHORT).show()
        }
        hideKeyboard()
    }

    private fun hideBottomBar() {

        val bottomBar = findViewById<LinearLayout>(R.id.bottomNavigation)
        buttonSearch.visibility = View.GONE
        searchView.visibility = View.GONE
        buttonViewToday.visibility = View.GONE

        bottomBar.visibility = View.GONE
    }

    private fun showBottomBar() {
        val bottomBar = findViewById<LinearLayout>(R.id.bottomNavigation)
        buttonSearch.visibility = View.VISIBLE
        searchView.visibility = View.VISIBLE
        buttonViewToday.visibility = View.VISIBLE
        bottomBar.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            showBottomBar()
        } else {
            super.onBackPressed()
        }
    }

}
