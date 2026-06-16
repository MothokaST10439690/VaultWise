package com.example.vaultwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val passwordHash: String, // Store hashes, not plain text!
    val createdAt: Long = System.currentTimeMillis(),
    val monthlyBudget: Double = 0.0, // Maximum goal
    val minMonthlyGoal: Double = 0.0 // Minimum goal
)
