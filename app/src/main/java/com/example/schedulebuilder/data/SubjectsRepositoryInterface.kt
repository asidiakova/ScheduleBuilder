package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [Subject].
 */
interface SubjectsRepositoryInterface {
    /**
     * Returns a flow of all subjects.
     */
    fun getAllSubjectsStream(): Flow<List<Subject>>

    /**
     * Inserts a [Subject] into the database.
     */
    suspend fun insertSubject(subject: Subject)

    /**
     * Deletes a [Subject] from the database.
     */
    suspend fun deleteSubject(subject: Subject)

    /**
     * Updates a [Subject] in the database.
     */
    suspend fun updateSubject(subject: Subject)
}