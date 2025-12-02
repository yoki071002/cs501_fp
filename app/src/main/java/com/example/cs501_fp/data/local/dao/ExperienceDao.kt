package com.example.cs501_fp.data.local.dao

import androidx.room.*
import com.example.cs501_fp.data.local.entity.Experience
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperienceDao {

    @Query("SELECT * FROM experiences")
    fun getAll(): Flow<List<Experience>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(exp: Experience): Long

    @Delete
    suspend fun deleteExperience(exp: Experience)
}