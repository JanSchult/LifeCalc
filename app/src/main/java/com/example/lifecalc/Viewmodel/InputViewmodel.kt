package com.example.lifecalc.Viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.R
import com.example.lifecalc.Viewmodel.UiState.InputUiState
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.billing.PremiumStatus
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.domain.model.UserProfile
import com.example.lifecalc.domain.usecase.CalculateLifetimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class InputViewModel @SuppressLint("StaticFieldLeak") constructor(
    private val calculateUseCase: CalculateLifetimeUseCase,
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences,
    private val context: Context

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
        _uiState.update { it.copy(incomeInput = value, incomeError = null) }
        viewModelScope.launch { userPreferences.saveIncome(value) }
    }


    fun onToggleIncomeType() {
        val newValue = !_uiState.value.isMonthly
        _uiState.update { it.copy(isMonthly = newValue) }
        viewModelScope.launch { userPreferences.saveIsMonthly(newValue) }
    }

    fun onHoursChange(value: String) {
        _uiState.update { it.copy(hoursPerWeek = value, hoursError = null) }
        viewModelScope.launch { userPreferences.saveHoursPerWeek(value) }
    }

    fun onTaxChange(value: String) {
        _uiState.update { it.copy(taxPercent = value, taxError = null) }
        viewModelScope.launch { userPreferences.saveTaxPercent(value) }
    }
    fun onTargetLabelChange(value: String) {
        _uiState.update { it.copy(targetLabel = value) }
    }

    fun onTargetAmountChange(value: String) {
        _uiState.update { it.copy(targetAmount = value, targetAmountError = null) }
    }

    fun calculate(sharedViewModel: SharedViewModel) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Premium-Check
                val isPremium = premiumStatus.value is PremiumStatus.Premium
                if (!isPremium) {
                    val count = userPreferences.getCalculationCount()
                    if (count >= FREE_CALCULATION_LIMIT) {
                        _uiState.update { it.copy(isLoading = false, showPaywall = true) }
                        return@launch
                    }
                }

                val income = _uiState.value.incomeInput.toDouble()
                val hours  = _uiState.value.hoursPerWeek.toDouble()
                val tax    = _uiState.value.taxPercent.toDoubleOrNull() ?: 0.0
                val target = _uiState.value.targetAmount.toDouble()

                val profile = if (_uiState.value.isMonthly)
                    UserProfile.fromMonthlyGross(income, hours, tax)
                else
                    UserProfile(
                        hourlyRateNet = income * (1 - tax / 100),
                        hoursPerWeek = hours,
                        taxDeductionPercent = tax
                    )

                val result = calculateUseCase(profile, target, _uiState.value.targetLabel)
                sharedViewModel.setResult(result)

                // Zähler erhöhen
                if (!isPremium) {
                    userPreferences.incrementCalculationCount()
                }

                _uiState.update { it.copy(isLoading = false, navigateToResult = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = context.getString(R.string.error_calculation_failed),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(showPaywall = false) }
    }

    companion object {
        const val FREE_CALCULATION_LIMIT = 2
    }


    /**
     * Validiert alle Felder und schreibt Fehler direkt in den UiState.
     * Gibt true zurück wenn alles valid ist.
     */
    private fun validate(): Boolean {
        val state = _uiState.value

        val incomeError = when {
            state.incomeInput.isBlank() ->
                context.getString(R.string.error_income_empty)
            state.incomeInput.toDoubleOrNull() == null ->
                context.getString(R.string.error_income_invalid)
            state.incomeInput.toDouble() <= 0 ->
                context.getString(R.string.error_income_zero)
            else -> null
        }

        val hoursError = when {
            state.hoursPerWeek.isBlank() ->
                context.getString(R.string.error_hours_empty)
            state.hoursPerWeek.toDoubleOrNull() == null ->
                context.getString(R.string.error_hours_invalid)
            state.hoursPerWeek.toDouble() <= 0 ->
                context.getString(R.string.error_hours_zero)
            state.hoursPerWeek.toDouble() > 168 ->
                context.getString(R.string.error_hours_max)
            else -> null
        }

        val taxError = when {
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDoubleOrNull() == null ->
                context.getString(R.string.error_tax_invalid)
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() < 0 ->
                context.getString(R.string.error_tax_negative)
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() >= 100 ->
                context.getString(R.string.error_tax_max)
            else -> null
        }

        val targetAmountError = when {
            state.targetAmount.isBlank() ->
                context.getString(R.string.error_amount_empty)
            state.targetAmount.toDoubleOrNull() == null ->
                context.getString(R.string.error_amount_invalid)
            state.targetAmount.toDouble() < 0 ->
                context.getString(R.string.error_amount_negative)
            state.targetAmount.toDouble() == 0.0 ->
                context.getString(R.string.error_amount_zero)
            else -> null
        }

        _uiState.update {
            it.copy(
                incomeError       = incomeError,
                hoursError        = hoursError,
                taxError          = taxError,
                targetAmountError = targetAmountError,
                error             = null
            )
        }

        return !_uiState.value.hasFieldErrors
    }
    fun resetNavigation() {
        _uiState.update { it.copy(navigateToResult = false) }
    }
}