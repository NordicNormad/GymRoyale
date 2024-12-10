package com.cs407.gymroyale

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val authButton = findViewById<Button>(R.id.buttonAuth)
        val toggleSwitch = findViewById<Switch>(R.id.switchAuthMode)
        val errorTextView = findViewById<TextView>(R.id.textViewError)

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                authButton.text = "Register"
                confirmPasswordEditText.visibility = EditText.VISIBLE
            } else {
                authButton.text = "Login"
                confirmPasswordEditText.visibility = EditText.GONE
            }
        }

        authButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Email and password are required."
                errorTextView.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (toggleSwitch.isChecked) {
                if (password != confirmPassword) {
                    errorTextView.text = "Passwords do not match."
                    errorTextView.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }
                registerUser(email, password, errorTextView)
            } else {
                loginUser(email, password, errorTextView)
            }
        }
    }
    private suspend fun readWorkoutsFromCsv(): List<String> {
        return withContext(Dispatchers.IO) {
            val workouts = mutableListOf<String>()
            val inputStream = resources.openRawResource(R.raw.workouts)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    workouts.add(line.trim())
                }
            }

            workouts
        }
    }

    private fun registerUser(email: String, password: String, errorTextView: TextView) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val userProfile = mapOf(
                        "name" to "New User",
                        "bio" to "Welcome to Gym Royale!",
                        "xp" to 0,
                        "challengesCompleted" to 0,
                        "trophies" to 0,
                        "workoutlog" to emptyList<Map<String, Any>>()
                    )
                    firestore.collection("users").document(userId).set(userProfile)
                        .addOnSuccessListener {
                            Log.d("LoginActivity", "Profile created successfully.")
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        }
                        .addOnFailureListener {
                            Log.e("LoginActivity", "Profile creation failed: ${it.message}")
                            errorTextView.text = "Profile creation failed."
                            errorTextView.visibility = TextView.VISIBLE
                        }
                } else {
                    Log.e("LoginActivity", "Registration failed: ${task.exception?.message}")
                    errorTextView.text = "Registration failed: ${task.exception?.message}"
                    errorTextView.visibility = TextView.VISIBLE
                }
            }
    }

    private fun loginUser(email: String, password: String, errorTextView: TextView) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
