package com.example.schedulebuilder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(teacher: Teacher)

    @Update
    suspend fun update(teacher: Teacher)

    @Delete
    suspend fun delete(teacher: Teacher)

    @Query("SELECT * from teachers")
    fun getAllTeachers(): Flow<List<Teacher>>
}