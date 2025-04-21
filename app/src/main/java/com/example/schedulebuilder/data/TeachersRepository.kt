package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

class TeachersRepository(private val teacherDao: TeacherDao) : TeachersRepositoryInterface {
//    override fun getAllTeachersStream(): Flow<List<Teacher>> = teacherDao.getAllTeachers()
    override fun getAllTeachersStream(): Flow<List<Teacher>> {
        val teachers =  teacherDao.getAllTeachers()
        return teachers
    }

    override suspend fun insertTeacher(teacher: Teacher) = teacherDao.insert(teacher)

    override suspend fun deleteTeacher(teacher: Teacher) = teacherDao.delete(teacher)

    override suspend fun updateTeacher(teacher: Teacher) = teacherDao.update(teacher)
}