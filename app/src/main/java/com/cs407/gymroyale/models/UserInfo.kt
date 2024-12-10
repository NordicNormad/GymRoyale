package com.cs407.gymroyale.models

// Firestore requires a no-argument constructor for deserialization
data class UserInfo(
    var bio: String = "",
    var challengesCompleted: Int = 0,
    var lookingForChallenge: Boolean = false,
    var name: String = "",
    var trophies: Int = 0,
    var xp: Int = 0
) {
    // No-argument constructor required for Firestore
    constructor() : this("", 0, false, "", 0, 0)
}