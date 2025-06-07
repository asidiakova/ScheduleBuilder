package com.example.schedulebuilder.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val scheduleEventsRepository: ScheduleEventsRepositoryInterface
    val subjectsRepository: SubjectsRepositoryInterface
    val teachersRepository: TeachersRepositoryInterface
    val locationsRepository: LocationsRepositoryInterface
}

/**
 * [AppContainer] implementation that provides instances of repositories.
 */
class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [ScheduleEventsRepositoryInterface]
     */
    override val scheduleEventsRepository: ScheduleEventsRepositoryInterface by lazy {
        ScheduleEventsRepository(ScheduleDatabase.getDatabase(context).scheduleEventDao(), context)
    }

    /**
     * Implementation for [SubjectsRepositoryInterface]
     */
    override val subjectsRepository: SubjectsRepositoryInterface by lazy {
        SubjectsRepository(ScheduleDatabase.getDatabase(context).subjectDao())
    }

    /**
     * Implementation for [TeachersRepositoryInterface]
     */
    override val teachersRepository: TeachersRepositoryInterface by lazy {
        TeachersRepository(ScheduleDatabase.getDatabase(context).teacherDao())
    }

    /**
     * Implementation for [LocationsRepositoryInterface]
     */
    override val locationsRepository: LocationsRepositoryInterface by lazy {
        LocationsRepository(ScheduleDatabase.getDatabase(context).locationDao())
    }

}