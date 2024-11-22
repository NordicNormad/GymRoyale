package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class LoadingScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading_screen, container, false)

        val textViewTip = view.findViewById<TextView>(R.id.textTip)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        // Display a random tip
        val tips = readTipsFromCsv()
        if (tips.isNotEmpty()) {
            val randomTip = tips[Random.nextInt(tips.size)]
            textViewTip.text = randomTip
        }

        // Cancel button onClick listener
        buttonCancel.setOnClickListener {
            parentFragmentManager.popBackStack() // Navigates back to the previous fragment (LandingPageFragment)
        }

        return view
    }

    // Function to read tips from the CSV file
    private fun readTipsFromCsv(): List<String> {
        val tips = mutableListOf<String>()
        val inputStream = resources.openRawResource(R.raw.tips)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.forEach { line ->
                tips.add(line.trim())
            }
        }

        return tips
    }
}
