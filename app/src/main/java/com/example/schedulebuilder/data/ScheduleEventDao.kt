package com.example.schedulebuilder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(scheduleEvent: ScheduleEvent)

    @Update
    suspend fun update(scheduleEvent: ScheduleEvent)

    @Delete
    suspend fun delete(scheduleEvent: ScheduleEvent)

    @Query("SELECT * from schedule_events WHERE id = :id")
    fun getScheduleEvent(id: Int): Flow<ScheduleEvent>

    @Query("SELECT * from schedule_events WHERE id = :id")
    fun getFullScheduleEvent(id: Int): Flow<FullScheduleEvent>

    @Query("SELECT * from schedule_events")
    fun getAllScheduleEvents(): Flow<List<ScheduleEvent>>

    @Query("SELECT * from schedule_events")
    fun getAllFullScheduleEvents(): Flow<List<FullScheduleEvent>>
}