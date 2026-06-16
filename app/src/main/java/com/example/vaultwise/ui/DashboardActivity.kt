package com.example.vaultwise.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vaultwise.R
import com.example.vaultwise.ui.fragments.AnalyticsFragment
import com.example.vaultwise.ui.fragments.BudgetGameFragment
import com.example.vaultwise.ui.fragments.HistoryFragment
import com.example.vaultwise.ui.fragments.LogExpenseFragment
import com.example.vaultwise.ui.fragments.SettingsFragment
import com.example.vaultwise.util.SettingsManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        SettingsManager(this).applyStoredLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set LogExpenseFragment as default (Home)
        if (savedInstanceState == null) {
            loadFragment(LogExpenseFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navigation_log_expense -> LogExpenseFragment()
                R.id.navigation_history -> HistoryFragment()
                R.id.navigation_analytics -> AnalyticsFragment()
                R.id.navigation_game -> BudgetGameFragment()
                R.id.navigation_settings -> SettingsFragment()
                else -> LogExpenseFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    fun openLogExpense() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.navigation_log_expense
    }
}
