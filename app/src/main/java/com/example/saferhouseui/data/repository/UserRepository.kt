package com.example.saferhouseui.data.repository

import com.example.saferhouseui.data.local.UserDao
import com.example.saferhouseui.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    fun getUserById(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
        // Future: Sync with Supabase
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        // Future: Sync with Supabase
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun updateLastSafeCheck(userId: String, timestamp: Long) {
        userDao.updateLastSafeCheck(userId, timestamp)
    }
}
