package com.example.lifecalc.Viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.R
import com.example.lifecalc.Viewmodel.UiState.OnboardingUiState
import com.example.lifecalc.data.preference.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferences: UserPreferences,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onIncomeChange(value: String) =
        _uiState.update { it.copy(incomeInput = value, incomeError = null) }

    fun onToggleIncomeType() =
        _uiState.update { it.copy(isMonthly = !it.isMonthly) }

    fun onHoursChange(value: String) =
        _uiState.update { it.copy(hoursPerWeek = value, hoursError = null) }

    fun onTaxChange(value: String) =
        _uiState.update { it.copy(taxPercent = value, taxError = null) }

    /**
     * Validiert, speichert in Preferences und markiert Onboarding als abgeschlossen.
     * Gibt true zurück wenn alles valid ist → NavController navigiert zu Input.
     */
    fun completeOnboarding(onSuccess: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            userPreferences.saveIncome(state.incomeInput)
            userPreferences.saveIsMonthly(state.isMonthly)
            userPreferences.saveHoursPerWeek(state.hoursPerWeek)
            userPreferences.saveTaxPercent(state.taxPercent)
            userPreferences.setOnboardingDone()

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

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

        _uiState.update {
            it.copy(
                incomeError = incomeError,
                hoursError  = hoursError,
                taxError    = taxError
            )
        }

        return incomeError == null && hoursError == null && taxError == null
    }
}