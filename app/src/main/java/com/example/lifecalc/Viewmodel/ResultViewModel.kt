package com.example.lifecalc.Viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.billing.PremiumStatus
import com.example.lifecalc.data.repository.CalculationRepository
import com.example.lifecalc.domain.model.CalculationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ResultViewModel erhält das CalculationResult über den SavedStateHandle.
 *
 * Der InputViewModel schreibt das Ergebnis per
 *   savedStateHandle["result"] = result
 * in den NavBackStackEntry — ResultViewModel liest es von dort.
 *
 * Alternativ: SharedViewModel über den NavGraph-Scope (beide Ansätze sind valide;
 * SavedStateHandle ist robuster gegen Process-Death).
 */
class ResultViewModel(
    private val repository: CalculationRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    val premiumStatus: StateFlow<PremiumStatus> = billingManager.premiumStatus

    private val FREE_SAVE_LIMIT = 3

    // Ergebnis kommt jetzt direkt vom SharedViewModel rein
    fun save(sharedViewModel: SharedViewModel) {
        val current = sharedViewModel.result.value ?: return
        if (_isSaved.value || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                val isPremium = premiumStatus.value is PremiumStatus.Premium

                if (!isPremium) {
                    val count = repository.getCount()
                    if (count >= FREE_SAVE_LIMIT) {
                        _saveError.value = PAYWALL_TRIGGER
                        return@launch
                    }
                }

                repository.save(current)
                _isSaved.value = true

            } catch (e: Exception) {
                _saveError.value = "Speichern fehlgeschlagen: ${e.localizedMessage}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _saveError.value = null
    }

    // isSaved zurücksetzen wenn neues Ergebnis kommt
    fun resetSavedState() {
        _isSaved.value = false
        _saveError.value = null
    }

    companion object {
        const val PAYWALL_TRIGGER = "PAYWALL"
    }
}