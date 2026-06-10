package com.example.lifecalc.Viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.Viewmodel.UiState.OnboardingUiState
import com.example.lifecalc.data.preference.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferences: UserPreferences
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
            state.incomeInput.isBlank()              -> "Bitte Einkommen eingeben"
            state.incomeInput.toDoubleOrNull() == null -> "Nur Zahlen erlaubt"
            state.incomeInput.toDouble() <= 0        -> "Muss größer als 0 sein"
            else -> null
        }

        val hoursError = when {
            state.hoursPerWeek.isBlank()               -> "Bitte Stunden eingeben"
            state.hoursPerWeek.toDoubleOrNull() == null -> "Nur Zahlen erlaubt"
            state.hoursPerWeek.toDouble() <= 0         -> "Muss größer als 0 sein"
            state.hoursPerWeek.toDouble() > 168        -> "Max. 168 Std./Woche"
            else -> null
        }

        val taxError = when {
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDoubleOrNull() == null  -> "Nur Zahlen erlaubt"
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() < 0            -> "Kann nicht negativ sein"
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() >= 100         -> "Muss unter 100% liegen"
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