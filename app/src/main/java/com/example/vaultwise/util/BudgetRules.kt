package com.example.vaultwise.util

import com.example.vaultwise.data.entity.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BudgetStatus {
    NOT_SET,
    BELOW_MIN,
    WITHIN_RANGE,
    OVER_MAX
}

data class RewardBadgeResult(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

object BudgetRules {

    fun isGoalRangeValid(minGoal: Double, maxGoal: Double): Boolean {
        return maxGoal > 0.0 && minGoal >= 0.0 && minGoal <= maxGoal
    }

    fun getBudgetStatus(spent: Double, minGoal: Double, maxGoal: Double): BudgetStatus {
        return when {
            maxGoal <= 0.0 -> BudgetStatus.NOT_SET
            spent > maxGoal -> BudgetStatus.OVER_MAX
            minGoal > 0.0 && spent < minGoal -> BudgetStatus.BELOW_MIN
            else -> BudgetStatus.WITHIN_RANGE
        }
    }

    fun isExpenseAmountValid(amount: Double): Boolean {
        return amount > 0.0
    }

    fun isTimeRangeValid(startTime: Long?, endTime: Long?): Boolean {
        return startTime == null || endTime == null || endTime >= startTime
    }

    fun filterExpensesInRange(expenses: List<Expense>, startDate: Long, endDate: Long): List<Expense> {
        return expenses.filter { it.date in startDate..endDate }
    }

    fun buildRewardBadges(
        expenses: List<Expense>,
        minGoal: Double,
        maxGoal: Double,
        now: Long = System.currentTimeMillis()
    ): List<RewardBadgeResult> {
        val currentMonthRange = currentMonthRange(now)
        val currentMonthExpenses = filterExpensesInRange(
            expenses,
            currentMonthRange.first,
            currentMonthRange.second
        )
        val monthlySpent = currentMonthExpenses.sumOf { it.amount }
        val uniqueLoggingDays = expenses.map { dayKey(it.date) }.distinct().size
        val loggedToday = expenses.any { dayKey(it.date) == dayKey(now) }
        val receiptCount = expenses.count { !it.photoUri.isNullOrBlank() }
        val categoryCountThisMonth = currentMonthExpenses.map { it.category }.distinct().size
        val status = getBudgetStatus(monthlySpent, minGoal, maxGoal)

        return listOf(
            RewardBadgeResult(
                title = "First Step",
                description = "Log your first expense.",
                unlocked = expenses.isNotEmpty()
            ),
            RewardBadgeResult(
                title = "Consistency Star",
                description = "Log at least one expense today.",
                unlocked = loggedToday
            ),
            RewardBadgeResult(
                title = "7-Day Logger",
                description = "Log expenses on 7 different days.",
                unlocked = uniqueLoggingDays >= 7
            ),
            RewardBadgeResult(
                title = "Under Max Hero",
                description = "Stay below your maximum monthly goal.",
                unlocked = maxGoal > 0.0 && monthlySpent <= maxGoal
            ),
            RewardBadgeResult(
                title = "Budget Keeper",
                description = "Stay between your minimum and maximum monthly goals.",
                unlocked = status == BudgetStatus.WITHIN_RANGE
            ),
            RewardBadgeResult(
                title = "Receipt Pro",
                description = "Attach 5 receipt photos to your expenses.",
                unlocked = receiptCount >= 5
            ),
            RewardBadgeResult(
                title = "Category Explorer",
                description = "Track expenses in 3 or more categories this month.",
                unlocked = categoryCountThisMonth >= 3
            )
        )
    }

    fun currentMonthRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis
        return start to end
    }

    fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun dayKey(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
