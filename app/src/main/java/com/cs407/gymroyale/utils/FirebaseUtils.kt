package com.cs407.gymroyale.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.cs407.gymroyale.models.UserInfo

object FirebaseUtils {
    fun loadUserInfoFromFirestore(onComplete: (UserInfo?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onComplete(null) // Return null if no user is logged in
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Convert Firestore document to UserInfo object
                    val userInfo = document.toObject(UserInfo::class.java)
                    onComplete(userInfo)
                } else {
                    onComplete(null) // Document doesn't exist
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(null) // Handle failure
            }
    }
}
