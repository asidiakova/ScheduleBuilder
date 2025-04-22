package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

class ScheduleEventsRepository(private val scheduleEventDao: ScheduleEventDao) : ScheduleEventsRepositoryInterface {
    override suspend fun insertScheduleEvent(scheduleEvent: ScheduleEvent) = scheduleEventDao.insert(scheduleEvent)

    override suspend fun deleteScheduleEvent(scheduleEvent: ScheduleEvent) = scheduleEventDao.delete(scheduleEvent)

    override suspend fun updateScheduleEvent(scheduleEvent: ScheduleEvent) = scheduleEventDao.update(scheduleEvent)

    override fun getScheduleEventStream(id: Int): Flow<ScheduleEvent?> = scheduleEventDao.getScheduleEvent(id)

    override fun getFullScheduleEventStream(id: Int): Flow<FullScheduleEvent?> = scheduleEventDao.getFullScheduleEvent(id)

    override fun getAllScheduleEventsStream(): Flow<List<ScheduleEvent>> = scheduleEventDao.getAllScheduleEvents()

    override fun getAllFullScheduleEventsStream(): Flow<List<FullScheduleEvent>>  = scheduleEventDao.getAllFullScheduleEvents()
}