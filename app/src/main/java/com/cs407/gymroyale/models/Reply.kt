package com.cs407.gymroyale.models

import java.util.Date

data class Reply(
    val userId: String = "",
    val name: String = "",
    val profileLink: String = "",
    val message: String = "",
    val timestamp: Date = Date()
){
    constructor() : this("", "", "", "", Date())
}
