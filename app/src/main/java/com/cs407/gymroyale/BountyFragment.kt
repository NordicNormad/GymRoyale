package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Bounty(val task: String, val completed: Boolean)

class BountyFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnClaimAll: Button
    private val bountyList = listOf(
        Bounty("Run 1 Mile", false),
        Bounty("30 Leg Lifts", true),
        Bounty("20 V-Ups", false),
        Bounty("50 Pushups", true)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bounty, container, false)

        recyclerView = view.findViewById(R.id.rvBounties)
        btnClaimAll = view.findViewById(R.id.btnClaimAll)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = BountyAdapter(bountyList)

        // Button click listener
        btnClaimAll.setOnClickListener {
            Toast.makeText(requireContext(), "All bounties claimed!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // Inner Adapter Class
    inner class BountyAdapter(private val bounties: List<Bounty>) :
        RecyclerView.Adapter<BountyAdapter.BountyViewHolder>() {

        inner class BountyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTask: TextView = view.findViewById(R.id.tvTask)
            val cbCompleted: CheckBox = view.findViewById(R.id.cbCompleted)
            val tvXP: TextView = view.findViewById(R.id.tvXP)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BountyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bounty, parent, false)
            return BountyViewHolder(view)
        }

        override fun onBindViewHolder(holder: BountyViewHolder, position: Int) {
            val bounty = bounties[position]
            holder.tvTask.text = bounty.task
            holder.cbCompleted.isChecked = bounty.completed
            holder.tvXP.text = "XP 50K"

            holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(
                    requireContext(),
                    if (isChecked) "Completed: ${bounty.task}" else "Uncompleted: ${bounty.task}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount() = bounties.size
    }
}
