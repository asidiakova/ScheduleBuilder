package com.example.schedulebuilder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ScheduleEvent::class, Subject::class, Teacher::class, Location::class],
    version = 2,
    exportSchema = false
)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleEventDao(): ScheduleEventDao
    abstract fun subjectDao(): SubjectDao
    abstract fun teacherDao(): TeacherDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var Instance: ScheduleDatabase? = null

        fun getDatabase(context: Context): ScheduleDatabase {
            return Instance ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context,
                    ScheduleDatabase::class.java,
                    "schedule_database"
                ).build()

                Instance = instance

                CoroutineScope(Dispatchers.IO).launch {

                    populateDatabase(instance)

                }
                instance

            }
        }

        private suspend fun populateDatabase(database: ScheduleDatabase) {

            database.teacherDao().insert(Teacher("Dr. Johnson"))
            database.teacherDao().insert(Teacher("Prof. Smith"))
            database.teacherDao().insert(Teacher("Dr. Garcia"))

            database.locationDao().insert(Location("A101"))
            database.locationDao().insert(Location("B205"))
            database.locationDao().insert(Location("C310"))

            database.subjectDao().insert(Subject("CS101", "Introduction to Programming"))
            database.subjectDao().insert(Subject("MATH202", "Calculus II"))
            database.subjectDao().insert(Subject("PHYS101", "Physics Fundamentals"))

        }
    }
}
