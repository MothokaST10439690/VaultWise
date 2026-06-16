package com.example.vaultwise.util

import com.example.vaultwise.data.entity.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetRulesTest {

    @Test
    fun minGoalCannotBeGreaterThanMaxGoal() {
        assertFalse(BudgetRules.isGoalRangeValid(minGoal = 3000.0, maxGoal = 2000.0))
    }

    @Test
    fun validGoalRangeReturnsTrue() {
        assertTrue(BudgetRules.isGoalRangeValid(minGoal = 800.0, maxGoal = 2500.0))
    }

    @Test
    fun spentBetweenMinAndMaxReturnsWithinRange() {
        assertEquals(
            BudgetStatus.WITHIN_RANGE,
            BudgetRules.getBudgetStatus(spent = 1500.0, minGoal = 1000.0, maxGoal = 2000.0)
        )
    }

    @Test
    fun spentAboveMaxReturnsOverMax() {
        assertEquals(
            BudgetStatus.OVER_MAX,
            BudgetRules.getBudgetStatus(spent = 2500.0, minGoal = 1000.0, maxGoal = 2000.0)
        )
    }

    @Test
    fun spentBelowMinReturnsBelowMin() {
        assertEquals(
            BudgetStatus.BELOW_MIN,
            BudgetRules.getBudgetStatus(spent = 500.0, minGoal = 1000.0, maxGoal = 2000.0)
        )
    }

    @Test
    fun endTimeCannotBeBeforeStartTime() {
        assertFalse(BudgetRules.isTimeRangeValid(startTime = 2000L, endTime = 1000L))
        assertTrue(BudgetRules.isTimeRangeValid(startTime = 1000L, endTime = 2000L))
    }

    @Test
    fun categoryExplorerBadgeUnlocksWithThreeCategoriesThisMonth() {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            testExpense(amount = 50.0, category = "Food", date = now),
            testExpense(amount = 70.0, category = "Transport", date = now),
            testExpense(amount = 100.0, category = "Health", date = now)
        )

        val badges = BudgetRules.buildRewardBadges(
            expenses = expenses,
            minGoal = 100.0,
            maxGoal = 1000.0,
            now = now
        )

        assertTrue(badges.first { it.title == "Category Explorer" }.unlocked)
    }

    @Test
    fun receiptProBadgeUnlocksAfterFiveReceipts() {
        val now = System.currentTimeMillis()
        val expenses = (1..5).map { index ->
            testExpense(amount = 20.0, category = "Food", date = now, photoUri = "receipt_$index.jpg")
        }

        val badges = BudgetRules.buildRewardBadges(
            expenses = expenses,
            minGoal = 0.0,
            maxGoal = 1000.0,
            now = now
        )

        assertTrue(badges.first { it.title == "Receipt Pro" }.unlocked)
    }

    private fun testExpense(
        amount: Double,
        category: String,
        date: Long,
        photoUri: String? = null
    ): Expense {
        return Expense(
            userId = 1,
            amount = amount,
            paymentMethod = "Digital",
            description = "Test expense",
            date = date,
            category = category,
            photoUri = photoUri
        )
    }
}
