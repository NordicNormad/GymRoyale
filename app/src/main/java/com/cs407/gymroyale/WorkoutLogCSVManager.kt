package com.cs407.gymroyale

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

class WorkoutLogCSVManager(private val context: Context) {

    private val fileName = "workout_logs.csv"
    private val TAG = "WorkoutLogCSVManager"

    private fun getWorkoutLogsFile(): File {
        val internalFile = File(context.filesDir, fileName)

        Log.d(TAG, "Internal file path: ${internalFile.absolutePath}")

        try {
            // Check if internal file exists
            if (!internalFile.exists()) {
                Log.d(TAG, "Internal file does not exist. Attempting to create.")

                // Try to copy from raw resource
                try {
                    val inputStream = context.resources.openRawResource(R.raw.workout_logs)
                    internalFile.createNewFile()
                    internalFile.outputStream().use { fileOut ->
                        inputStream.copyTo(fileOut)
                    }
                    Log.d(TAG, "Successfully copied workout_logs from raw resource")
                } catch (e: Exception) {
                    Log.e(TAG, "Error copying from raw resource", e)
                    // If raw resource doesn't exist, create a new file with headers
                    internalFile.createNewFile()
                    internalFile.writeText("TimeStamp,Workout Name,Weight,Reps,Date,Xp\n")
                    Log.d(TAG, "Created new file with default headers")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating internal file", e)
        }

        return internalFile
    }

    fun addWorkoutLog(workoutLog: WorkoutLog) {
        try {
            val file = getWorkoutLogsFile()
            Log.d(TAG, "Adding log: ${workoutLog}")

            FileWriter(file, true).use { writer ->
                val logEntry = "${workoutLog.timestamp},${workoutLog.workoutName},${workoutLog.weight},${workoutLog.reps},${workoutLog.date},${workoutLog.xp}\n"
                writer.append(logEntry)
                Log.d(TAG, "Log entry written: $logEntry")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding workout log", e)
        }
    }

    fun readWorkoutLogs(workoutName: String? = null): List<WorkoutLog> {
        try {
            val file = getWorkoutLogsFile()
            Log.d(TAG, "Reading logs. Workout name filter: $workoutName")

            return BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
                val logs = reader.readLines()
                    .drop(1) // Skip header
                    .mapNotNull { line ->
                        try {
                            val parts = line.split(",")
                            WorkoutLog(
                                timestamp = parts[0].toLong(),
                                workoutName = parts[1],
                                weight = parts[2].toDouble(),
                                reps = parts[3].toInt(),
                                date = parts[4],
                                xp = parts[5].toInt()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing log line: $line", e)
                            null
                        }
                    }
                    .let { logs ->
                        // Filter by workout name if provided
                        if (workoutName != null) {
                            logs.filter { it.workoutName == workoutName }
                        } else {
                            logs
                        }
                    }

                Log.d(TAG, "Read ${logs.size} logs")
                logs
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading workout logs", e)
            return emptyList()
        }
    }

    fun deleteWorkoutLog(workoutName: String, date: String, timestamp: Long) {
        try {
            val file = getWorkoutLogsFile()

            val logs = readWorkoutLogs()
                .filter { !(it.workoutName == workoutName && it.date == date && it.timestamp == timestamp) }

            // Rewrite file with remaining logs
            FileWriter(file).use { writer ->
                writer.write("TimeStamp,Workout Name,Weight,Reps,Date,Xp\n")
                logs.forEach { log ->
                    writer.append("${log.timestamp},${log.workoutName},${log.weight},${log.reps},${log.date},${log.xp}\n")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting workout log", e)
        }
    }

}