package com.cs407.gymroyale.models

data class Challenge(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var createdBy: String = "",
    var participants: List<String> = listOf(),
    var status: String = "open"
)
