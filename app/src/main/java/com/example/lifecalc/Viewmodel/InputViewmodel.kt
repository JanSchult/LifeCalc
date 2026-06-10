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
        // Erst validieren — wenn Fehler da sind, nicht weiterrechnen
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
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
                _uiState.update { it.copy(isLoading = false, navigateToResult = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Berechnung fehlgeschlagen.", isLoading = false) }
            }
        }
    }

    /**
     * Validiert alle Felder und schreibt Fehler direkt in den UiState.
     * Gibt true zurück wenn alles valid ist.
     */
    private fun validate(): Boolean {
        val state = _uiState.value

        // ── Einkommen ─────────────────────────────────────────
        val incomeError = when {
            state.incomeInput.isBlank() ->
                "Bitte Einkommen eingeben"
            state.incomeInput.toDoubleOrNull() == null ->
                "Nur Zahlen erlaubt"
            state.incomeInput.toDouble() <= 0 ->
                "Einkommen muss größer als 0 sein"
            else -> null
        }

        // ── Stunden ───────────────────────────────────────────
        val hoursError = when {
            state.hoursPerWeek.isBlank() ->
                "Bitte Stunden eingeben"
            state.hoursPerWeek.toDoubleOrNull() == null ->
                "Nur Zahlen erlaubt"
            state.hoursPerWeek.toDouble() <= 0 ->
                "Stunden müssen größer als 0 sein"
            state.hoursPerWeek.toDouble() > 168 ->
                "Maximal 168 Stunden/Woche möglich"
            else -> null
        }

        // ── Steuer ────────────────────────────────────────────
        val taxError = when {
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDoubleOrNull() == null ->
                "Nur Zahlen erlaubt"
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() < 0 ->
                "Abzüge können nicht negativ sein"
            state.taxPercent.isNotBlank() &&
                    state.taxPercent.toDouble() >= 100 ->
                "Abzüge müssen unter 100% liegen"
            else -> null
        }

        // ── Zielbetrag ────────────────────────────────────────
        val targetAmountError = when {
            state.targetAmount.isBlank() ->
                "Bitte Betrag eingeben"
            state.targetAmount.toDoubleOrNull() == null ->
                "Nur Zahlen erlaubt"
            state.targetAmount.toDouble() < 0 ->
                "Betrag kann nicht negativ sein"
            state.targetAmount.toDouble() == 0.0 ->
                "Betrag muss größer als 0 sein"
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