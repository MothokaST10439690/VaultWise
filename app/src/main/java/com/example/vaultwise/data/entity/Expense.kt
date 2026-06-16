package com.example.vaultwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val paymentMethod: String,
    val description: String,
    val date: Long,
    val startTime: Long? = null, // Store as timestamp
    val endTime: Long? = null,   // Store as timestamp
    val category: String,
    val photoUri: String? = null, // Path to stored photo
    val createdAt: Long = System.currentTimeMillis()
)
