package com.example.cs501_fp.data.repository

import com.example.cs501_fp.data.local.dao.UserEventDao
import com.example.cs501_fp.data.local.dao.ExperienceDao
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.local.entity.Experience
import kotlinx.coroutines.flow.Flow

class LocalRepository(
    private val userEventDao: UserEventDao,
    private val experienceDao: ExperienceDao
) {

    /* ------------------------------------------------------
     *                     USER EVENTS
     * ------------------------------------------------------ */

    /** 读取所有事件（实时监听 Flow） */
    fun getAllEvents(): Flow<List<UserEvent>> =
        userEventDao.getAllEvents()

    /** 本地添加 Event */
    suspend fun addEvent(event: UserEvent) =
        userEventDao.addEvent(event)

    /** 本地删除 Event */
    suspend fun deleteEvent(event: UserEvent) =
        userEventDao.deleteEvent(event)


    /* ------------------------------------------------------
     *                     EXPERIENCE
     * ------------------------------------------------------ */

    /** 读取所有 Experience（Flow） */
    fun getAllExperiences(): Flow<List<Experience>> =
        experienceDao.getAll()

    /** 本地添加 Experience */
    suspend fun addExperience(exp: Experience) =
        experienceDao.insertExperience(exp)

    /** 本地删除 Experience */
    suspend fun deleteExperience(exp: Experience) =
        experienceDao.deleteExperience(exp)

    suspend fun deleteAllEvents() = userEventDao.deleteAllEvents()
}