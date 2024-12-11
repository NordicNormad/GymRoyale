package com.cs407.gymroyale.models

data class Challenge(
    var id: String = "",
    var workout: String = "",               // Name of the workout
    var reps: Int = 0,                      // Number of repetitions
    var weight: Int = 0,                    // Weight in pounds (or units you're using)
    var trophies: Int = 0,                  // Number of trophies rewarded
    var createdBy: String = "",             // User ID of the challenge creator
    var completedBy: List<String> = emptyList(), // List of user IDs who completed the challenge
    var date: Any? = null,                   // Date when the challenge was created
    var comments: String = ""
)
