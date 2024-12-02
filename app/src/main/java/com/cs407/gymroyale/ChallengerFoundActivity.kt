package com.cs407.lab7

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.gymroyale.R

class ChallengerFoundFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_challenger_found_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)

        // ******************************************************* //
        // functionality to pull user pfp from DB call here
        // functionality to pull username from DB call here
        // ******************************************************* //


        // Handle Cancel button click
        buttonCancel.setOnClickListener {
            Toast.makeText(requireContext(), "Challenger screen canceled!", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Automatically close the fragment after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().supportFragmentManager.popBackStack()
        }, 5000)
    }
}
