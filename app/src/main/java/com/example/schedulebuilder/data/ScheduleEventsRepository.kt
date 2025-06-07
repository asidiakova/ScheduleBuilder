package com.example.schedulebuilder.data

import android.content.Context
import com.example.schedulebuilder.glance.updateScheduleWidget
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [ScheduleEvent].
 */
class ScheduleEventsRepository(
    private val scheduleEventDao: ScheduleEventDao,
    private val context: Context
) : ScheduleEventsRepositoryInterface {
    override suspend fun insertScheduleEvent(scheduleEvent: ScheduleEvent) {
        scheduleEventDao.insert(scheduleEvent)
        updateScheduleWidget(context)
    }

    override suspend fun deleteScheduleEvent(scheduleEvent: ScheduleEvent) {
        scheduleEventDao.delete(scheduleEvent)
        updateScheduleWidget(context)
    }

    override suspend fun deleteAllEvents() {
        scheduleEventDao.deleteAll()
        updateScheduleWidget(context)
    }

    override suspend fun updateScheduleEvent(scheduleEvent: ScheduleEvent) {
        scheduleEventDao.update(scheduleEvent)
        updateScheduleWidget(context)
    }

    override fun getScheduleEventStream(id: Int): Flow<ScheduleEvent?> =
        scheduleEventDao.getScheduleEvent(id)

    override fun getFullScheduleEventStream(id: Int): Flow<FullScheduleEvent?> =
        scheduleEventDao.getFullScheduleEvent(id)

    override fun getAllScheduleEventsStream(): Flow<List<ScheduleEvent>> =
        scheduleEventDao.getAllScheduleEvents()

    override fun getAllFullScheduleEventsStream(): Flow<List<FullScheduleEvent>> =
        scheduleEventDao.getAllFullScheduleEvents()
}