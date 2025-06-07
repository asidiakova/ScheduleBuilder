package com.example.schedulebuilder.data

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a full schedule event with associated [Teacher], [Location], and [Subject] information.
 */
data class FullScheduleEvent(
    @Embedded
    val scheduleEvent: ScheduleEvent,

    @Relation(
        parentColumn = "teacherName",
        entityColumn = "teacherName"
    )
    val teacher: Teacher,

    @Relation(
        parentColumn = "roomCode",
        entityColumn = "roomCode"
    )
    val location: Location,

    @Relation(
        parentColumn = "subjectShortenedCode",
        entityColumn = "shortenedCode"
    )
    val subject: Subject
)
