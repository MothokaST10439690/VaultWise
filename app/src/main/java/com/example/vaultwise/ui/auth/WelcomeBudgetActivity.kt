package com.example.vaultwise.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vaultwise.R
import com.example.vaultwise.databinding.ActivityWelcomeBudgetBinding
import com.example.vaultwise.ui.DashboardActivity
import com.example.vaultwise.ui.viewmodel.BudgetViewModel
import com.example.vaultwise.util.SettingsManager

class WelcomeBudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBudgetBinding
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        SettingsManager(this).applyStoredLanguage()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWelcomeBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnContinue.setOnClickListener {
            val minGoal = binding.etMinGoal.text.toString().toDoubleOrNull() ?: 0.0
            val maxGoal = binding.etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0

            if (maxGoal <= 0.0) {
                Toast.makeText(this, getString(R.string.error_max_goal_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoal > maxGoal) {
                Toast.makeText(this, getString(R.string.error_min_goal_greater_than_max), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            budgetViewModel.updateGoals(minGoal, maxGoal)
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
