package com.example.vaultwise.data.repository

import com.example.vaultwise.data.dao.UserDao
import com.example.vaultwise.data.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun updateGoals(userId: Int, minGoal: Double, maxGoal: Double) {
        userDao.updateGoals(userId, minGoal, maxGoal)
    }

    fun getUser(userId: Int): Flow<User?> {
        return userDao.getUserById(userId)
    }
}
