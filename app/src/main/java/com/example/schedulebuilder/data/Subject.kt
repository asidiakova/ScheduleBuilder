package com.example.schedulebuilder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = false)
    val shortenedCode: String,

    val fullDisplayName: String,
)
