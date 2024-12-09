package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Button
import android.content.Context
import android.content.SharedPreferences
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

data class UserInfo(
    val Username: String,
    val Level: Double,
    val Trophies: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val sharedPrefsName = "GymRoyalePrefs"
    private val userInfoKey = "UserInfo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase

        auth = FirebaseAuth.getInstance()
        Log.d("MainActivity", "Current user: ${auth.currentUser?.uid}")

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val userInfo = loadUserInfo(sharedPreferences) ?: createDefaultUserInfo(sharedPreferences)

        setContentView(R.layout.activity_main)

        // Load the main content (LandingPageFragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .commit()
        }
        // Set up the Profile button
        val profileButton = findViewById<Button>(R.id.buttonProfile)
        profileButton.setOnClickListener {
            handleProfileButtonClick()
        }
    }
        private fun handleProfileButtonClick() {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // Redirect to LoginActivity if user is not logged in
                Log.d("MainActivity", "No user logged in. Redirecting to LoginActivity.")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Load the ProfileFragment if the user is logged in
                Log.d("MainActivity", "User logged in: ${currentUser.email}. Loading ProfileFragment.")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .addToBackStack(null)
                    .commit()
            }
    }

    // loads user information into something
    private fun loadUserInfo(sharedPreferences: SharedPreferences): UserInfo? {
        val userInfoJson = sharedPreferences.getString(userInfoKey, null)
        return if (userInfoJson != null) {
            Gson().fromJson(userInfoJson, object : TypeToken<UserInfo>() {}.type)
        } else {
            null
        }
    }

    // saves user information into something
    private fun saveUserInfo(sharedPreferences: SharedPreferences, userInfo: UserInfo) {
        val editor = sharedPreferences.edit()
        val userInfoJson = Gson().toJson(userInfo)
        editor.putString(userInfoKey, userInfoJson)
        editor.apply()
    }

    // edits user information into the data structure
    private fun editUserInfo(
        sharedPreferences: SharedPreferences,
        username: String? = null,
        level: Double? = null,
        trophies: Int? = null
    ) {
        val currentUserInfo = loadUserInfo(sharedPreferences) ?: return
        val updatedUserInfo = currentUserInfo.copy(
            Username = username ?: currentUserInfo.Username,
            Level = level ?: currentUserInfo.Level,
            Trophies = trophies ?: currentUserInfo.Trophies
        )
        saveUserInfo(sharedPreferences, updatedUserInfo)

        Log.d("MainActivity", "UserInfo updated: $updatedUserInfo")
    }

    //////////////////////////////////////////////DELETE ME WHEN FIREBASE WORKS/////////////////////
    private fun createDefaultUserInfo(sharedPreferences: SharedPreferences): UserInfo {
        val defaultUserInfo = UserInfo(
            Username = "Player1",
            Level = 1.00001,
            Trophies = 0
        )
        saveUserInfo(sharedPreferences, defaultUserInfo)
        return defaultUserInfo
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}