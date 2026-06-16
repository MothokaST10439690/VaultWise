package com.example.vaultwise.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vaultwise.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesInRange(userId: Int, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND paymentMethod = :method")
    fun getTotalByPaymentMethod(userId: Int, method: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY category")
    fun getCategoryBreakdown(userId: Int): Flow<List<CategorySum>>

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

data class CategorySum(
    val category: String,
    val total: Double
)
