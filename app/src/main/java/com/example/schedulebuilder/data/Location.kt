package com.example.schedulebuilder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a location of the schedule event.
 */
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = false)
    val roomCode: String,
)
