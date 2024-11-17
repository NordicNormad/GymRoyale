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
import com.cs407.gymroyale.BountyFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.activity_main)

        // Check if there's an existing fragment
        if (savedInstanceState == null) {
            // Load BountyFragment into the container
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .commit()
        }
    }
}