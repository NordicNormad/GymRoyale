package com.cs407.gymroyale.models

import java.util.Date

data class Reply(
    var userId: String = "",
    var username: String = "",
    var message: String = "",
    var timestamp: Date = Date()
)
