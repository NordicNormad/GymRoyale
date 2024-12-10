package com.cs407.gymroyale.utils

import com.cs407.gymroyale.models.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    fun loadUserInfoFromFirestore(onComplete: (UserInfo?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(null)
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    onComplete(userInfo)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}
