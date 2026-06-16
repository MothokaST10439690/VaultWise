package com.example.vaultwise.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultwise.data.AppDatabase
import com.example.vaultwise.data.entity.User
import com.example.vaultwise.data.repository.UserRepository
import com.example.vaultwise.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val sessionManager = SessionManager(application)

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    private fun getCurrentUserId(): Int = sessionManager.getUserId()

    fun updateGoals(minGoal: Double, maxGoal: Double) {
        viewModelScope.launch {
            repository.updateGoals(getCurrentUserId(), minGoal, maxGoal)
        }
    }

    fun getUserData(): Flow<User?> {
        return repository.getUser(getCurrentUserId())
    }
}
