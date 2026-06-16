package com.example.vaultwise.data.dao

import androidx.room.*
import com.example.vaultwise.data.entity.CommittedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface CommittedExpenseDao {
    @Query("SELECT * FROM committed_expenses WHERE userId = :userId")
    fun getCommittedExpenses(userId: Int): Flow<List<CommittedExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: CommittedExpense)

    @Delete
    suspend fun delete(expense: CommittedExpense)
}
