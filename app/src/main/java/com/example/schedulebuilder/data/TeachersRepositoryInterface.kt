package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [Teacher].
 */
interface TeachersRepositoryInterface {

    /**
     * Returns a list of all teachers.
     */
    fun getAllTeachersStream(): Flow<List<Teacher>>

    /**
     * Inserts a [Teacher] into the database.
     */
    suspend fun insertTeacher(teacher: Teacher)

    /**
     * Deletes a [Teacher] from the database.
     */
    suspend fun deleteTeacher(teacher: Teacher)

    /**
     * Updates a [Teacher] in the database.
     */
    suspend fun updateTeacher(teacher: Teacher)
}