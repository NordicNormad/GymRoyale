package com.cs407.gymroyale.models

data class Challenge(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var status: String = "open",                // Challenge status
    var trophiesReward: Int = 20,               // Number of trophies awarded
    var completedBy: List<String> = emptyList(), // List of user IDs who completed the challenge
    var maxParticipants: Int = Int.MAX_VALUE   // Optional: max number of completions allowed
)
