package com.example.lifecalc.Viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.domain.model.UserProfile
import com.example.lifecalc.domain.usecase.CalculateLifetimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InputUiState(
    val incomeInput: String = "",
    val isMonthly: Boolean = true,
    val hoursPerWeek: String = "40",
    val taxPercent: String = "30",
    val targetAmount: String = "",
    val targetLabel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: CalculationResult? = null
)

class InputViewModel(
    private val calculateUseCase: CalculateLifetimeUseCase,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState.asStateFlow()

    val premiumStatus = billingManager.premiumStatus

    fun onIncomeChange(value: String) = _uiState.update { it.copy(incomeInput = value) }
    fun onToggleIncomeType() = _uiState.update { it.copy(isMonthly = !it.isMonthly) }
    fun onHoursChange(value: String) = _uiState.update { it.copy(hoursPerWeek = value) }
    fun onTaxChange(value: String) = _uiState.update { it.copy(taxPercent = value) }
    fun onTargetAmountChange(value: String) = _uiState.update { it.copy(targetAmount = value) }
    fun onTargetLabelChange(value: String) = _uiState.update { it.copy(targetLabel = value) }

    fun calculate() {
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
                _uiState.update { it.copy(result = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}