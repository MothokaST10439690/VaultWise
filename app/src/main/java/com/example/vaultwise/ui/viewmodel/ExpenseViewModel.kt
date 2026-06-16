package com.example.vaultwise.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultwise.data.AppDatabase
import com.example.vaultwise.data.dao.CategorySum
import com.example.vaultwise.data.entity.Expense
import com.example.vaultwise.data.repository.ExpenseRepository
import com.example.vaultwise.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val sessionManager = SessionManager(application)

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    private fun getCurrentUserId(): Int = sessionManager.getUserId()

    fun insert(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun getTotalByMethod(method: String): Flow<Double?> {
        return repository.getTotalByMethod(getCurrentUserId(), method)
    }

    fun getCategoryBreakdown(): Flow<List<CategorySum>> {
        return repository.getCategoryBreakdown(getCurrentUserId())
    }

    fun getAllExpenses(): Flow<List<Expense>> {
        return repository.getAllExpenses(getCurrentUserId())
    }

    fun getCurrentUserIdForExpense(): Int = getCurrentUserId()
}
