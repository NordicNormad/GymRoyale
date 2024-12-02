package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val profilePhoto = view.findViewById<ImageView>(R.id.imageViewProfilePhoto)
        val profileName = view.findViewById<TextView>(R.id.textViewProfileName)
        val profileBio = view.findViewById<TextView>(R.id.textViewProfileBio)
        val xpTextView = view.findViewById<TextView>(R.id.textViewXP)
        val challengesCompletedTextView = view.findViewById<TextView>(R.id.textViewChallengesCompleted)
        val logOutButton = view.findViewById<Button>(R.id.buttonLogOut)

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return view
        }

        val userId = auth.currentUser?.uid ?: ""

        // Fetch profile data from Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("ProfileFragment", "Document data: ${document.data}") // Log document content
                    profileName.text = document.getString("name") ?: "Name not set"
                    profileBio.text = document.getString("bio") ?: "Bio not set"
                    xpTextView.text = "XP: ${document.getLong("xp") ?: 0}"
                    challengesCompletedTextView.text =
                        "Challenges Completed: ${document.getLong("challengesCompleted") ?: 0}"
                } else {
                    Log.e("ProfileFragment", "Document is null or does not exist.")
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Error fetching profile: ${exception.message}")
                Toast.makeText(context, "Error fetching profile", Toast.LENGTH_SHORT).show()
            }


        // Log out functionality
        logOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}
