package com.example.schedulebuilder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = false)
    val teacherName: String,
)