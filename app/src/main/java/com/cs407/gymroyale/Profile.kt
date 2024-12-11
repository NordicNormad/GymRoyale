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
import com.cs407.gymroyalepackage.LandingPageFragment

class ProfileFragment : Fragment() {

    private lateinit var profilePhoto: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileBio: TextView
    private lateinit var uploadPhotoButton: Button
    private lateinit var logOutButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var bottomNavProfileButton: Button
    private lateinit var bottomNavHomeButton: Button
    private lateinit var bottomNavBountyButton: Button

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize UI elements
        profilePhoto = view.findViewById(R.id.imageViewProfilePhoto)
        profileName = view.findViewById(R.id.textViewProfileName)
        profileBio = view.findViewById(R.id.textViewProfileBio)
        uploadPhotoButton = view.findViewById(R.id.buttonUploadPhoto)
        logOutButton = view.findViewById(R.id.buttonLogOut)
        editProfileButton = view.findViewById(R.id.buttonEditProfile)
        bottomNavProfileButton = view.findViewById(R.id.buttonBottomNavProfile)
        bottomNavHomeButton = view.findViewById(R.id.buttonBottomNavHome)
        bottomNavBountyButton = view.findViewById(R.id.buttonBottomNavBounties)

        // Load the profile photo locally
        loadProfilePhotoLocally()

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
        setBottomNavigationListeners()

        return view
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
                profileName.text = newName
                profileBio.text = newBio
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun logOutUser() {
        Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        // Implement actual logout functionality here if needed
    }

    private fun setBottomNavigationListeners() {
        bottomNavProfileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .addToBackStack(null)
                .commit()
        }

        bottomNavBountyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BountyFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
