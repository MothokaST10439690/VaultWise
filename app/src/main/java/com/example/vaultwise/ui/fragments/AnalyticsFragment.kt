package com.example.vaultwise.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Pair as AndroidPair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vaultwise.R
import com.example.vaultwise.data.dao.CategorySum
import com.example.vaultwise.data.entity.Expense
import com.example.vaultwise.databinding.FragmentAnalyticsBinding
import com.example.vaultwise.ui.viewmodel.BudgetViewModel
import com.example.vaultwise.ui.viewmodel.CommittedExpenseViewModel
import com.example.vaultwise.ui.viewmodel.ExpenseViewModel
import com.example.vaultwise.util.BudgetRules
import com.example.vaultwise.util.BudgetStatus
import com.example.vaultwise.util.CategoryUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val committedViewModel: CommittedExpenseViewModel by viewModels()

    private val selectedPeriod = MutableStateFlow(defaultCurrentMonthRange())
    private val periodFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPeriodSelector()
        observeTotals()
        observeCategoryBreakdown()
        observeBudgetStatus()
        observeDailySpending()
    }

    private fun setupPeriodSelector() {
        updateSelectedPeriodLabel(selectedPeriod.value)

        binding.btnSelectPeriod.setOnClickListener {
            val currentRange = selectedPeriod.value
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.dialog_select_analytics_period))
                .setSelection(AndroidPair(currentRange.startDate, currentRange.endDate))
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val start = selection.first ?: return@addOnPositiveButtonClickListener
                val end = selection.second ?: return@addOnPositiveButtonClickListener
                val newRange = DateRange(
                    startDate = BudgetRules.startOfDay(start),
                    endDate = BudgetRules.endOfDay(end)
                )
                selectedPeriod.value = newRange
                updateSelectedPeriodLabel(newRange)
            }

            picker.show(parentFragmentManager, "ANALYTICS_PERIOD_PICKER")
        }
    }

    private fun observeTotals() {
        viewLifecycleOwner.lifecycleScope.launch {
            expenseViewModel.getAllExpenses().collectLatest { expenses ->
                val monthlyExpenses = expenses.currentMonthOnly()
                val digitalTotal = monthlyExpenses
                    .filter { it.paymentMethod.equals("Digital", ignoreCase = true) }
                    .sumOf { it.amount }
                val cashTotal = monthlyExpenses
                    .filter { it.paymentMethod.equals("Cash", ignoreCase = true) }
                    .sumOf { it.amount }

                binding.tvDigitalValue.text = String.format(Locale.getDefault(), "R %.2f", digitalTotal)
                binding.tvCashValue.text = String.format(Locale.getDefault(), "R %.2f", cashTotal)
            }
        }
    }

    private fun observeCategoryBreakdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                expenseViewModel.getAllExpenses(),
                budgetViewModel.getUserData(),
                selectedPeriod
            ) { expenses, user, range ->
                CategoryChartState(
                    expenses = BudgetRules.filterExpensesInRange(expenses, range.startDate, range.endDate),
                    minGoal = user?.minMonthlyGoal ?: 0.0,
                    maxGoal = user?.monthlyBudget ?: 0.0,
                    range = range
                )
            }.collectLatest { state ->
                updateSelectedPeriodLabel(state.range)
                updateCategoryChart(state)
            }
        }
    }

    private fun updateCategoryChart(state: CategoryChartState) {
        val breakdown = state.expenses
            .groupBy { it.category }
            .map { (category, categoryExpenses) ->
                CategorySum(category, categoryExpenses.sumOf { it.amount })
            }
            .sortedByDescending { it.total }

        val totalSum = breakdown.sumOf { it.total }
        binding.tvSelectedPeriodTotal.text = String.format(
            Locale.getDefault(),
            "Selected period spending: R %.2f",
            totalSum
        )
        binding.tvSelectedPeriodGoalRange.text = String.format(
            Locale.getDefault(),
            "Graph goals: Min R %.2f  •  Max R %.2f",
            state.minGoal,
            state.maxGoal
        )

        val status = BudgetRules.getBudgetStatus(totalSum, state.minGoal, state.maxGoal)
        val statusText = when (status) {
            BudgetStatus.NOT_SET -> getString(R.string.budget_status_not_set)
            BudgetStatus.BELOW_MIN -> getString(R.string.period_status_below_min)
            BudgetStatus.WITHIN_RANGE -> getString(R.string.period_status_within_range)
            BudgetStatus.OVER_MAX -> getString(R.string.period_status_over_max)
        }
        val statusColor = when (status) {
            BudgetStatus.NOT_SET -> resources.getColor(R.color.text_secondary, null)
            BudgetStatus.BELOW_MIN -> resources.getColor(R.color.warning, null)
            BudgetStatus.WITHIN_RANGE -> resources.getColor(R.color.button_green, null)
            BudgetStatus.OVER_MAX -> resources.getColor(R.color.danger, null)
        }
        binding.tvSelectedPeriodStatus.text = statusText
        binding.tvSelectedPeriodStatus.setTextColor(statusColor)

        binding.legendContainer.removeAllViews()

        if (breakdown.isEmpty()) {
            binding.chartDonut.setData(emptyList<CategorySum>())
            binding.chartDonut.progress = 0
            val tv = TextView(requireContext()).apply {
                text = getString(R.string.empty_data)
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            binding.legendContainer.addView(tv)
            return
        }

        binding.chartDonut.setData(breakdown)

        breakdown.forEach { item ->
            val percentage = if (totalSum > 0.0) (item.total / totalSum) * 100 else 0.0
            val tv = TextView(requireContext()).apply {
                text = String.format(
                    Locale.getDefault(),
                    "%s: R %.2f (%.0f%%)",
                    item.category,
                    item.total,
                    percentage
                )
                textSize = 12f
                setTextColor(CategoryUtils.getCategoryColor(requireContext(), item.category))
            }
            binding.legendContainer.addView(tv)
        }

        binding.chartDonut.progress = if (totalSum > 0.0) {
            ((breakdown.first().total / totalSum) * 100).toInt()
        } else {
            0
        }
    }

    private fun observeBudgetStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                budgetViewModel.getUserData(),
                expenseViewModel.getAllExpenses(),
                committedViewModel.getCommittedExpenses()
            ) { user, expenses, committed ->
                val maxGoal = user?.monthlyBudget ?: 0.0
                val minGoal = user?.minMonthlyGoal ?: 0.0
                val actualSpent = expenses.currentMonthOnly().sumOf { it.amount }
                val committedTotal = committed.sumOf { it.amount }
                val totalSpent = actualSpent + committedTotal
                BudgetSummaryState(
                    maxGoal = maxGoal,
                    minGoal = minGoal,
                    spent = totalSpent,
                    actual = actualSpent,
                    bills = committedTotal,
                    remaining = maxGoal - totalSpent
                )
            }.collectLatest { data ->
                binding.tvRemainingBudget.text = String.format(Locale.getDefault(), "R %.2f", data.remaining)
                binding.tvBillsTotal.text = String.format(Locale.getDefault(), "R %.2f", data.bills)
                binding.tvBudgetSpentLabel.text = String.format(
                    Locale.getDefault(),
                    "Past month: spent R %.2f of max R %.2f (bills included: R %.2f)",
                    data.actual,
                    data.maxGoal,
                    data.spent
                )
                binding.tvGoalRange.text = String.format(
                    Locale.getDefault(),
                    "Past-month goal range: R %.2f - R %.2f",
                    data.minGoal,
                    data.maxGoal
                )

                val status = BudgetRules.getBudgetStatus(data.spent, data.minGoal, data.maxGoal)
                if (data.maxGoal > 0.0) {
                    val progress = ((data.spent / data.maxGoal) * 100).toInt()
                    binding.progressBudget.progress = progress.coerceIn(0, 100)
                } else {
                    binding.progressBudget.progress = 0
                }

                when (status) {
                    BudgetStatus.NOT_SET -> setBudgetStatus(
                        getString(R.string.budget_status_not_set),
                        resources.getColor(R.color.text_secondary, null)
                    )
                    BudgetStatus.OVER_MAX -> setBudgetStatus(
                        getString(R.string.budget_status_over_max),
                        resources.getColor(R.color.danger, null)
                    )
                    BudgetStatus.BELOW_MIN -> setBudgetStatus(
                        getString(R.string.budget_status_below_min),
                        resources.getColor(R.color.warning, null)
                    )
                    BudgetStatus.WITHIN_RANGE -> setBudgetStatus(
                        getString(R.string.budget_status_within_range),
                        resources.getColor(R.color.button_green, null)
                    )
                }
            }
        }
    }

    private fun observeDailySpending() {
        viewLifecycleOwner.lifecycleScope.launch {
            expenseViewModel.getAllExpenses().collectLatest { expenses ->
                updateBarChart(expenses)
            }
        }
    }

    private fun updateBarChart(expenses: List<Expense>) {
        binding.chartBarContainer.removeAllViews()

        val calendar = Calendar.getInstance()
        val days = mutableListOf<DaySpend>()
        val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormatter = SimpleDateFormat("EEE", Locale.getDefault())

        repeat(7) {
            val dayStart = BudgetRules.startOfDay(calendar.timeInMillis)
            val dayEnd = BudgetRules.endOfDay(calendar.timeInMillis)
            val key = dayKeyFormatter.format(Date(dayStart))
            val label = labelFormatter.format(Date(dayStart))
            val total = expenses
                .filter { it.date in dayStart..dayEnd }
                .sumOf { it.amount }
            days.add(DaySpend(key, label, total))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val orderedDays = days.reversed()
        val maxAmount = orderedDays.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0

        orderedDays.forEach { day ->
            val barHeightRatio = (day.amount / maxAmount).toFloat()

            val barLayout = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val bar = View(requireContext()).apply {
                val height = (140 * barHeightRatio * resources.displayMetrics.density).toInt().coerceAtLeast(4)
                layoutParams = LinearLayout.LayoutParams(
                    (20 * resources.displayMetrics.density).toInt(),
                    height
                ).apply {
                    setMargins(4, 4, 4, 4)
                }
                setBackgroundColor(resources.getColor(R.color.button_green, null))
            }

            val label = TextView(requireContext()).apply {
                text = day.label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(resources.getColor(R.color.text_hint, null))
            }

            barLayout.addView(bar)
            barLayout.addView(label)
            binding.chartBarContainer.addView(barLayout)
        }
    }

    private fun updateSelectedPeriodLabel(range: DateRange) {
        binding.tvSelectedPeriod.text = String.format(
            Locale.getDefault(),
            "Selected graph period: %s - %s",
            periodFormatter.format(Date(range.startDate)),
            periodFormatter.format(Date(range.endDate))
        )
    }

    private fun List<Expense>.currentMonthOnly(): List<Expense> {
        val range = BudgetRules.currentMonthRange()
        return BudgetRules.filterExpensesInRange(this, range.first, range.second)
    }

    private fun setBudgetStatus(message: String, color: Int) {
        binding.progressBudget.setIndicatorColor(color)
        binding.tvRemainingBudget.setTextColor(color)
        binding.tvBudgetStatus.text = message
        binding.tvBudgetStatus.setTextColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class DateRange(val startDate: Long, val endDate: Long)

    private data class CategoryChartState(
        val expenses: List<Expense>,
        val minGoal: Double,
        val maxGoal: Double,
        val range: DateRange
    )

    private data class BudgetSummaryState(
        val maxGoal: Double,
        val minGoal: Double,
        val spent: Double,
        val actual: Double,
        val bills: Double,
        val remaining: Double
    )

    private data class DaySpend(
        val key: String,
        val label: String,
        val amount: Double
    )

    private companion object {
        fun defaultCurrentMonthRange(): DateRange {
            val range = BudgetRules.currentMonthRange()
            return DateRange(range.first, range.second)
        }
    }
}
