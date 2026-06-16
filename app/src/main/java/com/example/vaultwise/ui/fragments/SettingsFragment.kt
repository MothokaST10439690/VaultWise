package com.example.vaultwise.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vaultwise.MainActivity
import com.example.vaultwise.R
import com.example.vaultwise.data.entity.CommittedExpense
import com.example.vaultwise.databinding.FragmentSettingsBinding
import com.example.vaultwise.ui.viewmodel.AuthViewModel
import com.example.vaultwise.ui.viewmodel.BudgetViewModel
import com.example.vaultwise.ui.viewmodel.CommittedExpenseViewModel
import com.example.vaultwise.util.SettingsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val committedViewModel: CommittedExpenseViewModel by viewModels()
    private lateinit var settingsManager: SettingsManager
    private val profilePhotoPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val currentBinding = _binding ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            settingsManager.setProfilePhotoUri(uri.toString())
            currentBinding.ivProfilePhoto.setImageURI(uri)
            Toast.makeText(requireContext(), getString(R.string.message_profile_photo_updated), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        settingsManager = SettingsManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProfile()
        setupBudget()
        setupTheme()
        setupLanguage()
        setupCommittedExpenses()

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupProfile() {
        val username = authViewModel.getLoggedInUsername() ?: "User"
        binding.tvProfileName.text = username

        settingsManager.getProfilePhotoUri()?.let { savedUri ->
            binding.ivProfilePhoto.setImageURI(Uri.parse(savedUri))
        }

        binding.ivProfilePhoto.setOnClickListener {
            profilePhotoPicker.launch(arrayOf("image/*"))
        }

        binding.tvChangePhoto.setOnClickListener {
            profilePhotoPicker.launch(arrayOf("image/*"))
        }
    }

    private fun setupBudget() {
        viewLifecycleOwner.lifecycleScope.launch {
            budgetViewModel.getUserData().collectLatest { user ->
                if (user != null) {
                    binding.etMinGoal.setText(user.minMonthlyGoal.toInt().toString())
                    binding.etMaxGoal.setText(user.monthlyBudget.toInt().toString())
                }
            }
        }

        binding.btnSaveGoals.setOnClickListener {
            val minStr = binding.etMinGoal.text.toString().trim()
            val maxStr = binding.etMaxGoal.text.toString().trim()
            val min = minStr.toDoubleOrNull() ?: 0.0
            val max = maxStr.toDoubleOrNull() ?: 0.0

            if (max <= 0.0) {
                Toast.makeText(requireContext(), getString(R.string.error_max_goal_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (min > max) {
                Toast.makeText(requireContext(), getString(R.string.error_min_goal_greater_than_max), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            budgetViewModel.updateGoals(min, max)
            Toast.makeText(requireContext(), getString(R.string.message_goals_updated), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTheme() {
        binding.switchDarkMode.isChecked = settingsManager.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setDarkMode(isChecked)
        }
    }

    private fun setupLanguage() {
        val currentLang = settingsManager.getLanguage()
        when (currentLang) {
            "en" -> binding.btnLangEn.isChecked = true
            "af" -> binding.btnLangAf.isChecked = true
        }

        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val langCode = when (checkedId) {
                    R.id.btn_lang_en -> "en"
                    R.id.btn_lang_af -> "af"
                    else -> "en"
                }
                if (langCode != settingsManager.getLanguage()) {
                    settingsManager.setLanguage(langCode)
                    Toast.makeText(requireContext(), getString(R.string.message_language_updated), Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }
            }
        }
    }

    private fun setupCommittedExpenses() {
        viewLifecycleOwner.lifecycleScope.launch {
            committedViewModel.getCommittedExpenses().collectLatest { expenses ->
                updateCommittedExpensesUI(expenses)
            }
        }

        binding.btnAddBill.setOnClickListener {
            val name = binding.etBillName.text.toString().trim()
            val amountStr = binding.etBillAmount.text.toString().trim()
            val amount = amountStr.toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && amount > 0) {
                committedViewModel.addCommittedExpense(name, amount)
                binding.etBillName.text?.clear()
                binding.etBillAmount.text?.clear()
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_bill_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCommittedExpensesUI(expenses: List<CommittedExpense>) {
        binding.containerCommittedExpenses.removeAllViews()
        expenses.forEach { expense ->
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val tvName = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = expense.name
                setTextColor(resources.getColor(R.color.text_primary, null))
            }

            val tvAmount = TextView(requireContext()).apply {
                text = String.format(Locale.getDefault(), "R %.2f", expense.amount)
                setTextColor(resources.getColor(R.color.text_hint, null))
            }

            val ivDelete = ImageView(requireContext()).apply {
                val size = (20 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = (8 * resources.displayMetrics.density).toInt()
                }
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(resources.getColor(R.color.danger, null))
                setOnClickListener {
                    committedViewModel.deleteCommittedExpense(expense)
                }
            }

            row.addView(tvName)
            row.addView(tvAmount)
            row.addView(ivDelete)
            binding.containerCommittedExpenses.addView(row)
            
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
                setBackgroundColor(resources.getColor(R.color.border_color, null))
            }
            binding.containerCommittedExpenses.addView(divider)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
