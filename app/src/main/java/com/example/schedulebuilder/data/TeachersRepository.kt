package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [Teacher].
 */
class TeachersRepository(private val teacherDao: TeacherDao) : TeachersRepositoryInterface {
    override fun getAllTeachersStream(): Flow<List<Teacher>> = teacherDao.getAllTeachers()

    override suspend fun insertTeacher(teacher: Teacher) = teacherDao.insert(teacher)

    override suspend fun deleteTeacher(teacher: Teacher) = teacherDao.delete(teacher)

    override suspend fun updateTeacher(teacher: Teacher) = teacherDao.update(teacher)
}