package com.example.vaultwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "committed_expenses")
data class CommittedExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val amount: Double
)
