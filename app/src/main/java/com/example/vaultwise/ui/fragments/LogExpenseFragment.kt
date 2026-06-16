package com.example.vaultwise.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.vaultwise.R
import com.example.vaultwise.data.entity.Expense
import com.example.vaultwise.databinding.FragmentLogExpenseBinding
import com.example.vaultwise.ui.viewmodel.ExpenseViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogExpenseFragment : Fragment() {

    private var _binding: FragmentLogExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    
    private var selectedCategory: String = "Food"
    private var selectedDateTimestamp: Long = System.currentTimeMillis()
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var currentPhotoUri: Uri? = null
    private var photoPath: String? = null

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivExpensePhoto.visibility = View.VISIBLE
            binding.layoutPhotoPlaceholder.visibility = View.GONE
            binding.ivExpensePhoto.setImageURI(currentPhotoUri)
            photoPath = currentPhotoUri.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySelection()
        setupDatePicker()
        setupTimePickers()
        setupPhotoPicker()

        binding.btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupDatePicker() {
        binding.tilDate.editText?.setText(dateFormatter.format(Date(selectedDateTimestamp)))
        binding.tilDate.editText?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.dialog_select_date))
                .setSelection(selectedDateTimestamp)
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateTimestamp = selection
                binding.tilDate.editText?.setText(dateFormatter.format(Date(selectedDateTimestamp)))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupTimePickers() {
        binding.tilStartTime.editText?.setOnClickListener {
            showTimePicker { hour, minute ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedDateTimestamp
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                startTime = cal.timeInMillis
                binding.tilStartTime.editText?.setText(timeFormatter.format(cal.time))
            }
        }

        binding.tilEndTime.editText?.setOnClickListener {
            showTimePicker { hour, minute ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedDateTimestamp
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                endTime = cal.timeInMillis
                binding.tilEndTime.editText?.setText(timeFormatter.format(cal.time))
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (Int, Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(getString(R.string.dialog_select_time))
            .build()
        picker.addOnPositiveButtonClickListener {
            onTimeSelected(picker.hour, picker.minute)
        }
        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun setupPhotoPicker() {
        binding.cardPhotoPicker.setOnClickListener {
            val photoFile = File(requireContext().cacheDir, "expense_photo_${System.currentTimeMillis()}.jpg")
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(currentPhotoUri!!)
        }
    }

    private fun saveExpense() {
        val amountStr = binding.tilAmount.editText?.text?.toString() ?: ""
        val description = binding.tilDescription.editText?.text?.toString() ?: ""
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        
        val selectedPaymentId = binding.togglePayment.checkedButtonId
        val paymentMethod = if (selectedPaymentId == R.id.btn_cash) "Cash" else "Digital"

        val userId = viewModel.getCurrentUserIdForExpense()
        if (userId == -1) {
            Toast.makeText(requireContext(), getString(R.string.error_user_not_logged_in), Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0 || description.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_expense_required_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedStartTime = startTime
        val selectedEndTime = endTime
        if (selectedStartTime != null && selectedEndTime != null && selectedEndTime < selectedStartTime) {
            Toast.makeText(requireContext(), getString(R.string.error_end_time_before_start), Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            userId = userId,
            amount = amount,
            description = description,
            paymentMethod = paymentMethod,
            category = selectedCategory,
            date = selectedDateTimestamp,
            startTime = startTime,
            endTime = endTime,
            photoUri = photoPath
        )
        viewModel.insert(expense)
        Toast.makeText(requireContext(), getString(R.string.message_expense_saved), Toast.LENGTH_SHORT).show()
        resetFields()
    }

    private fun setupCategorySelection() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                if (chip != null && chip.id != R.id.chip_add_custom) {
                    selectedCategory = chip.text.toString()
                }
            }
        }
        binding.chipAddCustom.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val editText = EditText(requireContext())
        editText.hint = getString(R.string.hint_category_name)
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_add_category_title))
            .setView(editText)
            .setPositiveButton(getString(R.string.action_add)) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) addNewCategoryChip(name)
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun addNewCategoryChip(name: String) {
        val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle)
        chip.text = name
        chip.isCheckable = true
        chip.id = View.generateViewId()
        val index = binding.chipGroupCategories.indexOfChild(binding.chipAddCustom)
        binding.chipGroupCategories.addView(chip, index)
        chip.isChecked = true
        selectedCategory = name
    }

    private fun resetFields() {
        binding.tilAmount.editText?.text?.clear()
        binding.tilDescription.editText?.text?.clear()
        binding.tilStartTime.editText?.text?.clear()
        binding.tilEndTime.editText?.text?.clear()
        binding.ivExpensePhoto.visibility = View.GONE
        binding.layoutPhotoPlaceholder.visibility = View.VISIBLE
        startTime = null
        endTime = null
        photoPath = null
        selectedDateTimestamp = System.currentTimeMillis()
        binding.tilDate.editText?.setText(dateFormatter.format(Date(selectedDateTimestamp)))
        binding.chipFood.isChecked = true
        selectedCategory = "Food"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
