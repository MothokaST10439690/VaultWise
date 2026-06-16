package com.example.vaultwise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vaultwise.data.dao.UserDao
import com.example.vaultwise.data.dao.ExpenseDao
import com.example.vaultwise.data.dao.CommittedExpenseDao
import com.example.vaultwise.data.entity.User
import com.example.vaultwise.data.entity.Expense
import com.example.vaultwise.data.entity.CommittedExpense

@Database(entities = [User::class, Expense::class, CommittedExpense::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun committedExpenseDao(): CommittedExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vaultwise_database"
                )
                .fallbackToDestructiveMigration() // Simple for now, wipes DB on version change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
