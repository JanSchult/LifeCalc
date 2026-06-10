package com.example.lifecalc.Viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.domain.model.UserProfile
import com.example.lifecalc.domain.usecase.CalculateLifetimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class InputViewModel(
    private val calculateUseCase: CalculateLifetimeUseCase,
    billingManager: BillingManager,
    private val userPreferences: UserPreferences   // ← neu

) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    val premiumStatus = billingManager.premiumStatus

    init {
        // Gespeicherte Werte beim Start laden
        viewModelScope.launch {
            userPreferences.incomeFlow.collect { value ->
                _uiState.update { it.copy(incomeInput = value) }
            }
        }
        viewModelScope.launch {
            userPreferences.isMonthlyFlow.collect { value ->
                _uiState.update { it.copy(isMonthly = value) }
            }
        }
        viewModelScope.launch {
            userPreferences.hoursPerWeekFlow.collect { value ->
                _uiState.update { it.copy(hoursPerWeek = value) }
            }
        }
        viewModelScope.launch {
            userPreferences.taxPercentFlow.collect { value ->
                _uiState.update { it.copy(taxPercent = value) }
            }
        }
    }

    // Jede onChange-Funktion speichert sofort
    fun onIncomeChange(value: String) {
        _uiState.update { it.copy(incomeInput = value) }
        viewModelScope.launch { userPreferences.saveIncome(value) }
    }

    fun onToggleIncomeType() {
        val newValue = !_uiState.value.isMonthly
        _uiState.update { it.copy(isMonthly = newValue) }
        viewModelScope.launch { userPreferences.saveIsMonthly(newValue) }
    }

    fun onHoursChange(value: String) {
        _uiState.update { it.copy(hoursPerWeek = value) }
        viewModelScope.launch { userPreferences.saveHoursPerWeek(value) }
    }

    fun onTaxChange(value: String) {
        _uiState.update { it.copy(taxPercent = value) }
        viewModelScope.launch { userPreferences.saveTaxPercent(value) }
    }
    fun onTargetLabelChange(value: String) {
        _uiState.update { it.copy(targetLabel = value) }
    }

    fun onTargetAmountChange(value: String) {
        _uiState.update { it.copy(targetAmount = value) }
    }

    fun calculate(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val income = _uiState.value.incomeInput.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Ungültiges Einkommen")
                val hours = _uiState.value.hoursPerWeek.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Ungültige Stundenzahl")
                val tax = _uiState.value.taxPercent.toDoubleOrNull() ?: 0.0
                val target = _uiState.value.targetAmount.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Ungültiger Betrag")

                val profile = if (_uiState.value.isMonthly)
                    UserProfile.fromMonthlyGross(income, hours, tax)
                else
                    UserProfile(
                        hourlyRateNet = income * (1 - tax / 100),
                        hoursPerWeek = hours,
                        taxDeductionPercent = tax
                    )

                val result = calculateUseCase(profile, target, _uiState.value.targetLabel)

                // ← NEU: Ergebnis in SharedViewModel schreiben
                sharedViewModel.setResult(result)

                // ← GEÄNDERT: navigateToResult statt result im UiState
                _uiState.update { it.copy(isLoading = false, navigateToResult = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    fun resetNavigation() {
        _uiState.update { it.copy(navigateToResult = false) }
    }
}