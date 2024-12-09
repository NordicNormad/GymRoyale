package com.cs407.gymroyale.models

data class Challenge(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var uploadedBy: String = "",
    var uploadedByUsername: String? = null,
    var completedBy: String? = null,
    var completedByUsername: String? = null,
    var status: String = "open" // Status options: open, completed
)
