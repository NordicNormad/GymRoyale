package com.cs407.gymroyale.utils

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

object FirebaseUtils {
    fun loadUserInfoFromFirestore(onComplete: (com.cs407.gymroyale.models.UserInfo?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(null)
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val userInfo = document.toObject(com.cs407.gymroyale.models.UserInfo::class.java)
                        onComplete(userInfo)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(null)
                    }
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete(null)
            }
    }
    @SuppressLint("StaticFieldLeak")
    val db = FirebaseFirestore.getInstance()

    fun fetchUserProfile(userId: String, onComplete: (com.cs407.gymroyale.models.UserInfo?) -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                try {
                    if (document != null && document.exists()) {
                        val userInfo = document.toObject(com.cs407.gymroyale.models.UserInfo::class.java)
                        onComplete(userInfo)
                    } else {
                        Log.e("FirebaseUtils", "Document does not exist")
                        onComplete(null)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseUtils", "Deserialization error: ${e.message}")
                    e.printStackTrace()
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUtils", "Error fetching user profile: ${exception.message}")
                exception.printStackTrace()
                onComplete(null)
            }
    }
}




