package com.example.schedulebuilder.data

import android.content.Context

interface AppContainer {
    val scheduleEventsRepository: ScheduleEventsRepositoryInterface
    val subjectsRepository: SubjectsRepositoryInterface
    val teachersRepository: TeachersRepositoryInterface
    val locationsRepository: LocationsRepositoryInterface
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val scheduleEventsRepository: ScheduleEventsRepositoryInterface by lazy {
        ScheduleEventsRepository(ScheduleDatabase.getDatabase(context).scheduleEventDao())
    }

    override val subjectsRepository: SubjectsRepositoryInterface by lazy {
        SubjectsRepository(ScheduleDatabase.getDatabase(context).subjectDao())
    }

    override val teachersRepository: TeachersRepositoryInterface by lazy {
        TeachersRepository(ScheduleDatabase.getDatabase(context).teacherDao())
    }

    override val locationsRepository: LocationsRepositoryInterface by lazy {
        LocationsRepository(ScheduleDatabase.getDatabase(context).locationDao())
    }

}