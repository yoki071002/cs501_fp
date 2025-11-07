package com.example.cs501_fp.data.local

import androidx.room.*
import com.example.cs501_fp.data.model.User

/**
 * UserDao
 * -------------------------------------------------
 * Handles local user operations (registration, login, query).
 * Note: For Firebase Auth integration, userId should match Firebase UID.
 */

@Dao
interface UserDao {

    // ✅ 插入新用户（若存在相同 userId 则覆盖）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // ✅ 根据用户名和密码本地登录（仅用于离线模式）
    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // ✅ 根据用户名获取用户（检查是否存在）
    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // ✅ 根据 userId 获取用户（String 类型，与 Firebase UID 对应）
    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    // ✅ 删除用户（登出或账号删除时）
    @Delete
    suspend fun deleteUser(user: User)

    // ✅ 清空所有用户（调试或测试）
    @Query("DELETE FROM user")
    suspend fun clearAllUsers()
}
