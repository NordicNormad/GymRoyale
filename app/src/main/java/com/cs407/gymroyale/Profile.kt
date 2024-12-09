package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var profilePhotoUri: Uri? = null

    private val uploadImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                profilePhotoUri = uri
                uploadProfilePhoto(uri)
            }
        }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val profilePhoto = view.findViewById<ImageView>(R.id.imageViewProfilePhoto)
        val profileName = view.findViewById<TextView>(R.id.textViewProfileName)
        val profileBio = view.findViewById<TextView>(R.id.textViewProfileBio)
        val editBioButton = view.findViewById<Button>(R.id.buttonEditBio)
        val uploadPhotoButton = view.findViewById<Button>(R.id.buttonUploadPhoto)
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
                    Log.d("ProfileFragment", "Document data: ${document.data}")
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

        // Edit bio functionality
        editBioButton.setOnClickListener {
            val newBio = EditText(requireContext())
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Edit Bio")
                .setView(newBio)
                .setPositiveButton("Save") { _, _ ->
                    val bioText = newBio.text.toString()
                    firestore.collection("users").document(userId)
                        .update("bio", bioText)
                        .addOnSuccessListener {
                            profileBio.text = bioText
                            Toast.makeText(context, "Bio updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update bio.", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        // Upload photo functionality
        uploadPhotoButton.setOnClickListener {
            uploadImageLauncher.launch("image/*")
        }

        return view
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_photos/$userId.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile photo uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload photo.", Toast.LENGTH_SHORT).show()
            }
    }
}
