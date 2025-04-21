package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

interface SubjectsRepositoryInterface {

    fun getAllSubjectsStream(): Flow<List<Subject>>

    suspend fun insertSubject(subject: Subject)

    suspend fun deleteSubject(subject: Subject)

    suspend fun updateSubject(subject: Subject)
}