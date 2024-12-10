package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cs407.gymroyale.utils.FirebaseUtils
import com.cs407.gymroyalepackage.LandingPageFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var profilePhotoUri: Uri? = null
    private var viewedUserId: String? = null // To handle dynamic user IDs

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

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Retrieve UI elements
        val profilePhoto = view.findViewById<ImageView>(R.id.imageViewProfilePhoto)
        val profileName = view.findViewById<TextView>(R.id.textViewProfileName)
        val profileBio = view.findViewById<TextView>(R.id.textViewProfileBio)
        val uploadPhotoButton = view.findViewById<Button>(R.id.buttonUploadPhoto)
        val xpTextView = view.findViewById<TextView>(R.id.textViewXP)
        val trophyTextView = view.findViewById<TextView>(R.id.textTrophyXP)
        val challengesCompletedTextView = view.findViewById<TextView>(R.id.textViewChallengesCompleted)
        val logOutButton = view.findViewById<Button>(R.id.buttonLogOut)
        val editProfileButton = view.findViewById<Button>(R.id.buttonEditProfile)

        val bottomNavProfileButton = view.findViewById<Button>(R.id.buttonBottomNavProfile)
        val bottomNavBountyButton = view.findViewById<Button>(R.id.buttonBottomNavBounties)
        val bottomNavHomeButton = view.findViewById<Button>(R.id.buttonBottomNavHome)

        // Get userId from arguments or fallback to the logged-in user
        viewedUserId = arguments?.getString("userId") ?: auth.currentUser?.uid

        if (viewedUserId == null) {
            Toast.makeText(context, "No user information available", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return view
        }

        // Hide edit and upload buttons if viewing another user's profile
        if (viewedUserId != auth.currentUser?.uid) {
            uploadPhotoButton.visibility = View.GONE
            editProfileButton.visibility = View.GONE
        }

        // Fetch and display profile data
        fetchProfileData(viewedUserId!!, profileName, profileBio, xpTextView, trophyTextView, challengesCompletedTextView)

        // Button listeners
        logOutButton.setOnClickListener { logOutUser() }
        uploadPhotoButton.setOnClickListener { uploadImageLauncher.launch("image/*") }
        editProfileButton.setOnClickListener { showEditProfileDialog(profileName, profileBio, viewedUserId!!) }

        // Bottom navigation
        setBottomNavigationListeners(bottomNavProfileButton, bottomNavBountyButton, bottomNavHomeButton)

        return view
    }

    private fun fetchProfileData(
        userId: String,
        profileName: TextView,
        profileBio: TextView,
        xpTextView: TextView,
        trophyTextView: TextView,
        challengesCompletedTextView: TextView
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profileName.text = document.getString("name") ?: "Name not set"
                    profileBio.text = document.getString("bio") ?: "Bio not set"
                    xpTextView.text = "XP: ${document.getLong("xp") ?: 0}"
                    trophyTextView.text = "Trophies: ${document.getLong("trophies") ?: 0}"
                    challengesCompletedTextView.text =
                        "Challenges Completed: ${document.getLong("challengesCompleted") ?: 0}"
                } else {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Error fetching profile data", e)
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun logOutUser() {
        auth.signOut()
        navigateToLogin()
    }

    private fun setBottomNavigationListeners(
        profileButton: Button,
        bountyButton: Button,
        homeButton: Button
    ) {
        profileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        bountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }

        homeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }
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

    private fun showEditProfileDialog(profileName: TextView, profileBio: TextView, userId: String) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        val nameField = EditText(requireContext())
        nameField.hint = "Name"
        val bioField = EditText(requireContext())
        bioField.hint = "Bio"
        layout.addView(nameField)
        layout.addView(bioField)
        nameField.setText(profileName.text)
        bioField.setText(profileBio.text)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameField.text.toString()
                val newBio = bioField.text.toString()
                val updates = hashMapOf<String, Any>(
                    "name" to newName,
                    "bio" to newBio
                )
                firestore.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        profileName.text = newName
                        profileBio.text = newBio
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}
