package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [Subject].
 */
class SubjectsRepository(private val subjectDao: SubjectDao) : SubjectsRepositoryInterface {
    override fun getAllSubjectsStream(): Flow<List<Subject>> = subjectDao.getAllSubjects()

    override suspend fun insertSubject(subject: Subject) = subjectDao.insert(subject)

    override suspend fun deleteSubject(subject: Subject) = subjectDao.delete(subject)

    override suspend fun updateSubject(subject: Subject) = subjectDao.update(subject)

}