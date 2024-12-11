package com.cs407.gymroyale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymroyalepackage.LandingPageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Bounty(val task: String, var isSelected: Boolean = false)

class BountyFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClaimBounty: Button
    private lateinit var bountyAdapter: BountyAdapter
    private var bountyList = mutableListOf<Bounty>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bounty, container, false)


        val bottomNavProfileButton = view.findViewById<Button>(R.id.buttonBottomNavProfile)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)

        recyclerView = view.findViewById(R.id.rvBounties)
        btnClaimBounty = view.findViewById(R.id.btnClaimAll)

        // Load workouts from CSV
        loadWorkouts()

        // Disable claim button if already claimed today
        if (hasClaimedToday()) {
            btnClaimBounty.isEnabled = false
            Toast.makeText(requireContext(), "You've already claimed a workout today! Come back tomorrow.", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bountyAdapter = BountyAdapter(bountyList)
        recyclerView.adapter = bountyAdapter

        // Button click listener for claiming a bounty
        btnClaimBounty.setOnClickListener {
            val selectedBounty = bountyAdapter.getSelectedBounty()
            if (selectedBounty != null) {
                // Navigate to SearchWorkout with the selected workout
                val intent = Intent(requireContext(), SearchWorkout::class.java)
                intent.putExtra("SELECTED_WORKOUT", selectedBounty.task)
                intent.putExtra("IS_BOUNTY", true)
                startActivity(intent)

                // Save today's date after claiming
                saveLastClaimDate()

                // Disable claim button for the rest of the day
                btnClaimBounty.isEnabled = false
                Toast.makeText(requireContext(), "You've successfully claimed a workout! Come back tomorrow.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please select a workout first", Toast.LENGTH_SHORT).show()
            }
        }

        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavProfileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
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

    // Function to get today's date as a String
    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Function to save the last claim date in SharedPreferences
    private fun saveLastClaimDate() {
        val sharedPreferences = requireContext().getSharedPreferences("BountyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("lastClaimDate", getTodayDate())  // Save today's date
        editor.apply()
    }

    // Function to check if the last claim date is today
    private fun hasClaimedToday(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("BountyPrefs", Context.MODE_PRIVATE)
        val lastClaimDate = sharedPreferences.getString("lastClaimDate", null)
        val todayDate = getTodayDate()
        return lastClaimDate == todayDate
    }

    private fun loadWorkouts() {
        // If the user hasn't claimed today, allow them to see new bounties
        if (!hasClaimedToday()) {
            lifecycleScope.launch {
                bountyList.clear()
                val workouts = readWorkoutsFromCsv()
                bountyList.addAll(workouts.map { Bounty(it) })
                bountyAdapter.notifyDataSetChanged()
            }
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

            workouts.shuffled().take(3)
        }
    }

    // Inner Adapter Class
    inner class BountyAdapter(private val bounties: List<Bounty>) :
        RecyclerView.Adapter<BountyAdapter.BountyViewHolder>() {

        // Keep track of the currently selected position
        private var selectedPosition = -1

        inner class BountyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTask: TextView = view.findViewById(R.id.tvTask)
            val cbCompleted: CheckBox = view.findViewById(R.id.cbCompleted)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BountyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bounty, parent, false)
            return BountyViewHolder(view)
        }

        override fun onBindViewHolder(holder: BountyViewHolder, position: Int) {
            val bounty = bounties[position]
            holder.tvTask.text = bounty.task

            // Check or uncheck the checkbox based on the current selection
            holder.cbCompleted.isChecked = (position == selectedPosition)

            // Handle checkbox clicks for selection
            holder.cbCompleted.setOnClickListener {
                // Update selected position
                val previousSelectedPosition = selectedPosition
                selectedPosition = if (selectedPosition == position) -1 else position

                // Notify changes to refresh the view for both the old and new selection
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }

        override fun getItemCount() = bounties.size

        // Return the selected bounty if any
        fun getSelectedBounty(): Bounty? {
            return if (selectedPosition != -1) bounties[selectedPosition] else null
        }
    }

}