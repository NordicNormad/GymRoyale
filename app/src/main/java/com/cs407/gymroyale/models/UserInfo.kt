package com.cs407.gymroyale.models

data class UserInfo(
    val name: String = "Anonymous",       // User's display name
    val bio: String = "",                 // User's bio
    val xp: Int = 0,                      // Experience points
    val challengesCompleted: Int = 0,     // Number of challenges completed
    val level: Double = 0.0,              // User's level
    val trophies: Int = 0                 // User's trophies
)
