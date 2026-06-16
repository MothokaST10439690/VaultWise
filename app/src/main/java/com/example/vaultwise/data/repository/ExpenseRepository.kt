package com.example.vaultwise.data.repository

import com.example.vaultwise.data.dao.CategorySum
import com.example.vaultwise.data.dao.ExpenseDao
import com.example.vaultwise.data.entity.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    
    fun getAllExpenses(userId: Int): Flow<List<Expense>> {
        return expenseDao.getAllExpenses(userId)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    fun getTotalByMethod(userId: Int, method: String): Flow<Double?> {
        return expenseDao.getTotalByPaymentMethod(userId, method)
    }

    fun getCategoryBreakdown(userId: Int): Flow<List<CategorySum>> {
        return expenseDao.getCategoryBreakdown(userId)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}
