package com.example.schedulebuilder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a schedule event.
 */
@Entity(
    tableName = "schedule_events", foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["teacherName"],
            childColumns = ["teacherName"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["roomCode"],
            childColumns = ["roomCode"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["shortenedCode"],
            childColumns = ["subjectShortenedCode"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("teacherName"),
        Index("roomCode"),
        Index("subjectShortenedCode")
    ]
)
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val teacherName: String,
    val roomCode: String,
    val subjectShortenedCode: String,

    val obligation: Obligation,
    val day: Int,
    val startHour: Int,
    val endHour: Int
)
