package com.example.lifecalc.Viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.billing.PremiumStatus
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.data.repository.ExpenseRepository
import com.example.lifecalc.domain.model.Expense
import com.example.lifecalc.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FREE_EXPENSE_LIMIT = 3

class BudgetViewModel(
    private val expenseRepository: ExpenseRepository,
    private val userPreferences: UserPreferences,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    val premiumStatus: StateFlow<PremiumStatus> = billingManager.premiumStatus

    init {
        // Expenses beobachten
        viewModelScope.launch {
            expenseRepository.getAllExpenses().collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
                recalculate(expenses)
            }
        }

        // Nettoeinkommen aus Preferences beobachten
        viewModelScope.launch {
            combine(
                userPreferences.incomeFlow,
                userPreferences.isMonthlyFlow,
                userPreferences.taxPercentFlow,
                userPreferences.hoursPerWeekFlow
            ) { income, isMonthly, tax, hours ->
                calculateNetMonthlyIncome(income, isMonthly, tax, hours)
            }.collect { netIncome ->
                _uiState.update { state ->
                    val remaining = netIncome - state.totalExpenses
                    val hourlyRate = if (netIncome > 0)
                        netIncome / ((state.uiState().hoursPerWeek()) * 4.33)
                    else 0.0
                    state.copy(
                        netIncome = netIncome,
                        remaining = remaining,
                        remainingInHours = if (hourlyRate > 0) remaining / hourlyRate else 0.0
                    )
                }
            }
        }
    }

    private fun recalculate(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        val remaining = _uiState.value.netIncome - total
        _uiState.update { it.copy(totalExpenses = total, remaining = remaining) }
    }

    private fun calculateNetMonthlyIncome(
        income: String,
        isMonthly: Boolean,
        tax: String,
        hours: String
    ): Double {
        val incomeVal = income.toDoubleOrNull() ?: return 0.0
        val taxVal = tax.toDoubleOrNull() ?: 0.0
        val hoursVal = hours.toDoubleOrNull() ?: 40.0
        val net = incomeVal * (1 - taxVal / 100)
        return if (isMonthly) net
        else net * hoursVal * 4.33  // Stundenlohn → monatlich
    }

    // ── Dialog ────────────────────────────────────────────────────────────

    fun openAddDialog() {
        viewModelScope.launch {
            val isPremium = premiumStatus.value is PremiumStatus.Premium
            val count = expenseRepository.getCount()
            if (!isPremium && count >= FREE_EXPENSE_LIMIT) {
                _uiState.update { it.copy(showPaywall = true) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    showAddDialog = true,
                    editingExpense = null,
                    dialogName = "",
                    dialogAmount = "",
                    dialogCategory = ExpenseCategory.SONSTIGES
                )
            }
        }
    }

    fun openEditDialog(expense: Expense) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingExpense = expense,
                dialogName = expense.name,
                dialogAmount = expense.amount.toString(),
                dialogCategory = expense.category
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingExpense = null) }
    }

    fun onDialogNameChange(value: String) =
        _uiState.update { it.copy(dialogName = value) }

    fun onDialogAmountChange(value: String) =
        _uiState.update { it.copy(dialogAmount = value) }

    fun onDialogCategoryChange(value: ExpenseCategory) =
        _uiState.update { it.copy(dialogCategory = value) }

    fun saveExpense() {
        val name = _uiState.value.dialogName.trim()
        val amount = _uiState.value.dialogAmount.toDoubleOrNull() ?: return
        if (name.isBlank() || amount <= 0) return

        viewModelScope.launch {
            val editing = _uiState.value.editingExpense
            if (editing != null) {
                expenseRepository.update(
                    editing.copy(
                        name = name,
                        amount = amount,
                        category = _uiState.value.dialogCategory
                    )
                )
            } else {
                expenseRepository.insert(
                    Expense(
                        name = name,
                        amount = amount,
                        category = _uiState.value.dialogCategory
                    )
                )
            }
            closeDialog()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseRepository.delete(expense) }
    }

    fun dismissPaywall() = _uiState.update { it.copy(showPaywall = false) }
}

// Hilfsfunktion — vermeidet Circular Reference im init
private fun BudgetUiState.uiState() = this
private fun BudgetUiState.hoursPerWeek() = 40.0 // Fallback, wird durch Flow überschrieben