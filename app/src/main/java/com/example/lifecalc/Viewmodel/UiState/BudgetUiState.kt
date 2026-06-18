package com.example.lifecalc.Viewmodel.UiState

import com.example.lifecalc.domain.model.Expense
import com.example.lifecalc.domain.model.ExpenseCategory

data class BudgetUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenses: Double = 0.0,
    val netIncome: Double = 0.0,
    val remaining: Double = 0.0,
    val remainingInHours: Double = 0.0,
    val showAddDialog: Boolean = false,
    val editingExpense: Expense? = null,
    val showPaywall: Boolean = false,
    val exportState: ExportState = ExportState.Idle,    // ← neu

    // Add-Dialog Felder
    val dialogName: String = "",
    val dialogAmount: String = "",
    val dialogCategory: ExpenseCategory = ExpenseCategory.SONSTIGES
)