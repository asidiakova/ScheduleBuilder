package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

interface ScheduleEventsRepositoryInterface {
    suspend fun insertScheduleEvent(scheduleEvent: ScheduleEvent)

    suspend fun deleteScheduleEvent(scheduleEvent: ScheduleEvent)

    suspend fun updateScheduleEvent(scheduleEvent: ScheduleEvent)

    fun getScheduleEventStream(id: Int): Flow<ScheduleEvent?>

    fun getFullScheduleEventStream(id: Int): Flow<FullScheduleEvent?>

    fun getAllScheduleEventsStream(): Flow<List<ScheduleEvent>>

    fun getAllFullScheduleEventsStream(): Flow<List<FullScheduleEvent>>


}