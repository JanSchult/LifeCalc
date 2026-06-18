package com.example.lifecalc.Viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.R
import com.example.lifecalc.Viewmodel.UiState.BudgetUiState
import com.example.lifecalc.Viewmodel.UiState.ExportState
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
    private val billingManager: BillingManager,
    private val context: Context                    // ← neu
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    val premiumStatus: StateFlow<PremiumStatus> = billingManager.premiumStatus

    init {
        viewModelScope.launch {
            expenseRepository.getAllExpenses().collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
                recalculate(expenses)
            }
        }

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
        return if (isMonthly) net else net * hoursVal * 4.33
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

    fun showPaywall() = _uiState.update { it.copy(showPaywall = true) }

    // ── Export (Premium) ────────────────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportCsv() {
        val isPremium = premiumStatus.value is PremiumStatus.Premium
        if (!isPremium) {
            _uiState.update { it.copy(showPaywall = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(exportState = ExportState.InProgress) }
            try {
                val csv = buildCsvContent(_uiState.value.expenses)
                val uri = writeCsvToDownloads(csv, "lifecalc_budget.csv")
                _uiState.update {
                    it.copy(exportState = ExportState.Success(uri.toString()))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        exportState = ExportState.Error(
                            e.localizedMessage ?: "Export fehlgeschlagen"
                        )
                    )
                }
            }
        }
    }

    private fun buildCsvContent(expenses: List<Expense>): String {
        val header = context.getString(R.string.csv_budget_header) + "\n"
        val rows = expenses.joinToString("\n") { e ->
            "\"${e.name}\",\"${context.getString(e.category.stringRes)}\",${e.amount}"
        }
        return header + rows
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeCsvToDownloads(content: String, fileName: String): android.net.Uri {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = resolver.insert(
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Datei konnte nicht erstellt werden")

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }

        contentValues.clear()
        contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return uri
    }

    fun clearExportState() {
        _uiState.update { it.copy(exportState = ExportState.Idle) }
    }
}

// Hilfsfunktion — vermeidet Circular Reference im init
private fun BudgetUiState.uiState() = this
private fun BudgetUiState.hoursPerWeek() = 40.0 // Fallback, wird durch Flow überschrieben