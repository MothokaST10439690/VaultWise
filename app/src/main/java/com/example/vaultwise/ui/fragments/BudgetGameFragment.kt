package com.example.vaultwise.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vaultwise.R
import com.example.vaultwise.databinding.FragmentBudgetGameBinding
import com.example.vaultwise.ui.viewmodel.BudgetViewModel
import com.example.vaultwise.ui.viewmodel.ExpenseViewModel
import com.example.vaultwise.util.BudgetRules
import com.example.vaultwise.util.RewardBadgeResult
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BudgetGameFragment : Fragment() {

    private var _binding: FragmentBudgetGameBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()

    private lateinit var cells: List<MaterialButton>
    private val board = MutableList(BOARD_SIZE) { "" }
    private var gameOver = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGameBoard()
        observeRewardBadges()

        binding.btnNewRound.setOnClickListener {
            resetGame()
        }
    }

    private fun setupGameBoard() {
        cells = listOf(
            binding.cell0,
            binding.cell1,
            binding.cell2,
            binding.cell3,
            binding.cell4,
            binding.cell5,
            binding.cell6,
            binding.cell7,
            binding.cell8
        )

        cells.forEachIndexed { index, button ->
            button.setOnClickListener {
                playUserMove(index)
            }
        }

        resetGame()
    }

    private fun observeRewardBadges() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                budgetViewModel.getUserData(),
                expenseViewModel.getAllExpenses()
            ) { user, expenses ->
                BudgetRules.buildRewardBadges(
                    expenses = expenses,
                    minGoal = user?.minMonthlyGoal ?: 0.0,
                    maxGoal = user?.monthlyBudget ?: 0.0
                )
            }.collectLatest { badges ->
                renderRewardBadges(badges)
            }
        }
    }

    private fun renderRewardBadges(badges: List<RewardBadgeResult>) {
        binding.badgeContainer.removeAllViews()
        val earnedCount = badges.count { it.unlocked }
        binding.tvRewardSummary.text = getString(
            R.string.reward_summary_format,
            earnedCount,
            badges.size
        )

        badges.forEach { badge ->
            val badgeView = TextView(requireContext()).apply {
                text = if (badge.unlocked) {
                    "✓ ${badge.title}\n${badge.description}"
                } else {
                    "Locked: ${badge.title}\n${badge.description}"
                }
                textSize = 13f
                setTextColor(
                    resources.getColor(
                        if (badge.unlocked) R.color.button_green else R.color.text_secondary,
                        null
                    )
                )
                setBackgroundResource(
                    if (badge.unlocked) R.drawable.bg_earned_tag else R.drawable.bg_locked_tag
                )
                val padding = (12 * resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (10 * resources.displayMetrics.density).toInt())
                }
            }
            binding.badgeContainer.addView(badgeView)
        }
    }

    private fun playUserMove(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return

        markCell(index, USER_MARK)

        when {
            hasWinner(USER_MARK) -> finishGame(getString(R.string.money_match_win))
            board.isFull() -> finishGame(getString(R.string.money_match_draw))
            else -> playAppMove()
        }
    }

    private fun playAppMove() {
        val move = findBestMove(APP_MARK)
            ?: findBestMove(USER_MARK)
            ?: board.indices.firstOrNull { board[it].isEmpty() }
            ?: return

        markCell(move, APP_MARK)

        when {
            hasWinner(APP_MARK) -> finishGame(getString(R.string.money_match_loss))
            board.isFull() -> finishGame(getString(R.string.money_match_draw))
            else -> binding.tvMatchStatus.text = getString(R.string.money_match_turn)
        }
    }

    private fun findBestMove(mark: String): Int? {
        return WIN_LINES.firstNotNullOfOrNull { line ->
            val values = line.map { board[it] }
            if (values.count { it == mark } == 2 && values.count { it.isEmpty() } == 1) {
                line.first { board[it].isEmpty() }
            } else {
                null
            }
        }
    }

    private fun markCell(index: Int, mark: String) {
        board[index] = mark
        cells[index].text = mark
        cells[index].isEnabled = false
        cells[index].setTextColor(resources.getColor(R.color.button_green, null))
    }

    private fun hasWinner(mark: String): Boolean {
        return WIN_LINES.any { line ->
            line.all { board[it] == mark }
        }
    }

    private fun finishGame(message: String) {
        gameOver = true
        binding.tvMatchStatus.text = message
        cells.forEach { it.isEnabled = false }
    }

    private fun resetGame() {
        gameOver = false
        board.indices.forEach { index ->
            board[index] = ""
            cells[index].text = HABITS[index]
            cells[index].isEnabled = true
            cells[index].setTextColor(resources.getColor(R.color.text_primary, null))
        }
        binding.tvMatchStatus.text = getString(R.string.money_match_turn)
    }

    private fun List<String>.isFull(): Boolean {
        return all { it.isNotEmpty() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val BOARD_SIZE = 9
        const val USER_MARK = "X"
        const val APP_MARK = "O"

        val HABITS = listOf(
            "Save",
            "Track",
            "Plan",
            "Pause",
            "Budget",
            "Review",
            "Limit",
            "Compare",
            "Avoid"
        )

        val WIN_LINES = listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )
    }
}
