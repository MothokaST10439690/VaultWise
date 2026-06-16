package com.example.vaultwise.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vaultwise.data.entity.Expense
import com.example.vaultwise.databinding.FragmentHistoryBinding
import com.example.vaultwise.ui.DashboardActivity
import com.example.vaultwise.databinding.ItemExpenseBinding
import com.example.vaultwise.ui.viewmodel.ExpenseViewModel
import com.example.vaultwise.util.CategoryUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    private var startDate: Long? = null
    private var endDate: Long? = null
    private var allExpenses: List<Expense> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExpenseAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        observeExpenses()

        binding.btnFilterDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.fabAddExpense.setOnClickListener {
            (activity as? DashboardActivity)?.openLogExpense()
        }
    }

    private fun observeExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllExpenses().collectLatest { expenses ->
                allExpenses = expenses
                applyHistoryFilter()
            }
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(com.example.vaultwise.R.string.dialog_select_date_range))
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            startDate = startOfDay(selection.first)
            endDate = endOfDay(selection.second)
            applyHistoryFilter()
        }
        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun applyHistoryFilter() {
        val start = startDate ?: Long.MIN_VALUE
        val end = endDate ?: Long.MAX_VALUE
        val filtered = allExpenses.filter { expense ->
            expense.date in start..end
        }

        adapter.submitList(filtered)
        binding.layoutEmptyHistory.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvHistory.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    inner class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {
        private var items = listOf<Expense>()

        fun submitList(newItems: List<Expense>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val itemBinding: ItemExpenseBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            fun bind(expense: Expense) {
                itemBinding.tvItemDescription.text = expense.description
                itemBinding.tvItemAmount.text = String.format(Locale.getDefault(), "R %.2f", expense.amount)
                
                // Color by category
                val color = CategoryUtils.getCategoryColor(itemView.context, expense.category)
                itemBinding.viewCategoryColor.setBackgroundColor(color)

                val dateStr = dateFormatter.format(Date(expense.date))
                val timeStr = if (expense.startTime != null) {
                    timeFormatter.format(Date(expense.startTime))
                } else {
                    getString(com.example.vaultwise.R.string.not_available)
                }
                itemBinding.tvItemDetails.text = getString(
                    com.example.vaultwise.R.string.history_item_details_format,
                    expense.category,
                    dateStr,
                    timeStr
                )

                if (expense.photoUri != null) {
                    itemBinding.ivPhotoThumbnail.setImageURI(Uri.parse(expense.photoUri))
                } else {
                    itemBinding.ivPhotoThumbnail.setImageResource(android.R.drawable.ic_menu_camera)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
