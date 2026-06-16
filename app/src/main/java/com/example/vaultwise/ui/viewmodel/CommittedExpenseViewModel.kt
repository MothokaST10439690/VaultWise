package com.example.vaultwise.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultwise.data.AppDatabase
import com.example.vaultwise.data.entity.CommittedExpense
import com.example.vaultwise.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CommittedExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).committedExpenseDao()
    private val sessionManager = SessionManager(application)

    fun getCommittedExpenses(): Flow<List<CommittedExpense>> {
        return dao.getCommittedExpenses(sessionManager.getUserId())
    }

    fun addCommittedExpense(name: String, amount: Double) {
        viewModelScope.launch {
            dao.insert(CommittedExpense(userId = sessionManager.getUserId(), name = name, amount = amount))
        }
    }

    fun deleteCommittedExpense(expense: CommittedExpense) {
        viewModelScope.launch {
            dao.delete(expense)
        }
    }
}
