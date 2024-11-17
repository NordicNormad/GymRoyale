package com.cs407.gymroyale
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var rvBounties: RecyclerView
    private lateinit var btnClaimAll: Button
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.activity_main)

        rvBounties = findViewById(R.id.rvBounties)
        btnClaimAll = findViewById(R.id.btnClaimAll)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        val bounties = listOf(
            Bounty("Run 1 Mile", true),
            Bounty("30 Leg Lifts", true),
            Bounty("20 V-Ups", false)
        )

        rvBounties.layoutManager = LinearLayoutManager(this)
        rvBounties.adapter = BountyAdapter(bounties)

        btnClaimAll.setOnClickListener {
            // Implement claim all logic here
        }

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            // Handle navigation item selection
            true
        }
    }
}

data class Bounty(val task: String, val completed: Boolean)

class BountyAdapter(private val bounties: List<Bounty>) :
    RecyclerView.Adapter<BountyAdapter.BountyViewHolder>() {

    class BountyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
    }

    override fun getItemCount() = bounties.size
}