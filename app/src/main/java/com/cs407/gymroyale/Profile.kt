package com.cs407.gymroyale

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cs407.gymroyalepackage.LandingPageFragment

class ProfileFragment : Fragment() {
    private fun navigateFragment(fragment: Fragment, slideInFromRight: Boolean) {
        val (enter, exit, popEnter, popExit) = if (slideInFromRight) {
            arrayOf(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else {
            arrayOf(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit, popEnter, popExit)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profilePhoto: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileBio: TextView
    private lateinit var xpTextView: TextView
    private lateinit var trophyTextView: TextView
    private lateinit var challengesCompletedTextView: TextView
    private lateinit var uploadPhotoButton: Button
    private lateinit var logOutButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var bottomNavProfileButton: Button
    private lateinit var bottomNavHomeButton: Button
    private lateinit var bottomNavBountyButton: Button
    private var viewedUserId: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI elements
        profilePhoto = view.findViewById(R.id.imageViewProfilePhoto)
        profileName = view.findViewById(R.id.textViewProfileName)
        profileBio = view.findViewById(R.id.textViewProfileBio)
        xpTextView = view.findViewById(R.id.textViewXP)
        trophyTextView = view.findViewById(R.id.textTrophyXP)
        challengesCompletedTextView = view.findViewById(R.id.textViewChallengesCompleted)
        uploadPhotoButton = view.findViewById(R.id.buttonUploadPhoto)
        logOutButton = view.findViewById(R.id.buttonLogOut)
        editProfileButton = view.findViewById(R.id.buttonEditProfile)
        bottomNavProfileButton = view.findViewById(R.id.buttonBottomNavProfile)
        bottomNavHomeButton = view.findViewById(R.id.buttonBottomNavHome)
        bottomNavBountyButton = view.findViewById(R.id.buttonBottomNavBounties)
        // Get userId from arguments or fallback to the logged-in user
        viewedUserId = arguments?.getString("userId") ?: auth.currentUser?.uid

        if (viewedUserId == null) {
            Toast.makeText(context, "No user information available", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return view
        }

        // Load profile photo locally
        loadProfilePhotoLocally()

        // Fetch and display profile data from Firebase
        fetchProfileData(viewedUserId!!)

        // Set up button listeners
        uploadPhotoButton.setOnClickListener {
            ProfilePictureDialog { selectedPictureResId ->
                profilePhoto.setImageResource(selectedPictureResId) // Show the selected photo immediately
                saveSelectedResourceLocally(selectedPictureResId) // Save it locally
            }.show(parentFragmentManager, "ProfilePictureDialog")
        }

        logOutButton.setOnClickListener {
            logOutUser()
        }

        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        // Set up bottom navigation
        bottomNavBountyButton.setOnClickListener {
            navigateFragment(BountyFragment(), slideInFromRight = true)   // left to right
        }

        bottomNavHomeButton.setOnClickListener {
            navigateFragment(LandingPageFragment(), slideInFromRight = true)  // right to left
        }

        return view
    }

    private fun fetchProfileData(userId: String) {
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

    private fun saveSelectedResourceLocally(imageResId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, imageResId)
        val fileName = "profile_photo.jpg" // Unique file name for the profile photo

        try {
            requireContext().openFileOutput(fileName, android.content.Context.MODE_PRIVATE).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            Log.d("ProfileFragment", "Profile photo saved locally: $fileName")
            Toast.makeText(context, "Profile photo saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error saving profile photo locally: ${e.message}")
            Toast.makeText(context, "Failed to save profile photo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePhotoLocally() {
        val fileName = "profile_photo.jpg"

        try {
            val fileInputStream = requireContext().openFileInput(fileName)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            profilePhoto.setImageBitmap(bitmap)
            Log.d("ProfileFragment", "Profile photo loaded from local storage.")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading profile photo: ${e.message}")
            profilePhoto.setImageResource(R.drawable.ic_profile_placeholder) // Fallback to default
        }
    }

    private fun showEditProfileDialog() {
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
                firestore.collection("users").document(viewedUserId!!)
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

    private fun logOutUser() {
        auth.signOut()
        Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
