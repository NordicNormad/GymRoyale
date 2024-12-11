package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class UserProfileDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the dialog layout
        return inflater.inflate(R.layout.dialog_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val profileNameTextView = view.findViewById<TextView>(R.id.profileNameTextView)
        val profileStatsTextView = view.findViewById<TextView>(R.id.profileStatsTextView)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        // Get user data passed via arguments
        val userName = arguments?.getString("userName") ?: "Unknown"
        val userStats = arguments?.getString("userStats") ?: "No stats available"

        // Populate the dialog
        profileNameTextView.text = userName
        profileStatsTextView.text = userStats

        // Set close button action
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(userName: String, userStats: String): UserProfileDialog {
            val dialog = UserProfileDialog()
            val args = Bundle()
            args.putString("userName", userName)
            args.putString("userStats", userStats)
            dialog.arguments = args
            return dialog
        }
    }
}
