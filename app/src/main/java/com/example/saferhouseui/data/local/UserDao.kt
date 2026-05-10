package com.example.saferhouseui.data.local

import androidx.room.*
import com.example.saferhouseui.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET lastSafeCheck = :timestamp WHERE id = :userId")
    suspend fun updateLastSafeCheck(userId: String, timestamp: Long)
}
