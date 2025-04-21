package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

interface TeachersRepositoryInterface {
    fun getAllTeachersStream(): Flow<List<Teacher>>

    suspend fun insertTeacher(teacher: Teacher)

    suspend fun deleteTeacher(teacher: Teacher)

    suspend fun updateTeacher(teacher: Teacher)
}