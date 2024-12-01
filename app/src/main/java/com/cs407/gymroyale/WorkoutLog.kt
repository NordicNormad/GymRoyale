package com.cs407.gymroyale

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoutLog(
    val id: Int,
    val workoutName: String,
    val weight: Double,
    val reps: Int,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)