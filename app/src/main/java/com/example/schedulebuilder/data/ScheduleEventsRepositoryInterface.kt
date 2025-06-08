package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [ScheduleEvent].
 */
interface ScheduleEventsRepositoryInterface {
    /**
     * Inserts [ScheduleEvent] into the database.
     */
    suspend fun insertScheduleEvent(scheduleEvent: ScheduleEvent)

    /**
     * Deletes [ScheduleEvent] from the database.
     */
    suspend fun deleteScheduleEvent(scheduleEvent: ScheduleEvent)

    /**
     * Deletes all [ScheduleEvent] objects from the database.
     */
    suspend fun deleteAllEvents()

    /**
     * Updates [ScheduleEvent] in the database.
     */
    suspend fun updateScheduleEvent(scheduleEvent: ScheduleEvent)

    /**
     * Returns an optional flow containing the list of all ScheduleEvents from the database.
     */
    fun getScheduleEventStream(id: Int): Flow<ScheduleEvent?>

    /**
     * Returns an optional flow containing the list of all Full ScheduleEvents from the database.
     */
    fun getFullScheduleEventStream(id: Int): Flow<FullScheduleEvent?>

    /**
     * Returns a flow containing the list of all ScheduleEvents from the database.
     */
    fun getAllScheduleEventsStream(): Flow<List<ScheduleEvent>>

    /**
     * Returns a flow containing the list of all Full ScheduleEvents from the database.
     */
    fun getAllFullScheduleEventsStream(): Flow<List<FullScheduleEvent>>
}