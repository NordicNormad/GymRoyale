package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cs407.gymroyale.R

class ChallengerFoundActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenger_found_loading)

        val progressBar: ProgressBar = findViewById(R.id.progressBarLoading)
        val loadingMessage: TextView = findViewById(R.id.textLoadingMessage)

        // Simulate loading (e.g., fetching data)
        Handler().postDelayed({
            // Transition to the detailed view
            startActivity(Intent(this, ChallengerFoundDetailActivity::class.java))
            finish()
        }, 3000) // Delay for 3 seconds
    }
}
