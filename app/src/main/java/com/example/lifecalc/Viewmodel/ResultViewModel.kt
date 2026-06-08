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
    savedStateHandle: SavedStateHandle,
    private val repository: CalculationRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────

    /**
     * Das Ergebnis kommt über den SavedStateHandle aus dem InputViewModel.
     * Key muss mit dem Key im NavGraph übereinstimmen ("result").
     */
    private val _result = MutableStateFlow<CalculationResult?>(
        savedStateHandle["result"]
    )
    val result: StateFlow<CalculationResult?> = _result.asStateFlow()

    /** Ob der aktuelle Eintrag bereits in der DB gespeichert wurde. */
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    /** Ob das Speichern gerade läuft (verhindert Doppelklick). */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    /** Fehlermeldung beim Speichern (z.B. DB-Fehler). */
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    /** Premium-Status für eventuelle UI-Gating-Logik im Result-Screen. */
    val premiumStatus: StateFlow<PremiumStatus> = billingManager.premiumStatus

    // ── Free-Tier-Grenze ─────────────────────────────────────────────────

    /**
     * Im Free-Tier darf der Nutzer max. 3 Einträge speichern.
     * Bei Überschreitung: Paywall zeigen statt speichern.
     */
    private val FREE_SAVE_LIMIT = 3

    // ── Öffentliche Aktionen ──────────────────────────────────────────────

    /**
     * Speichert das aktuelle Ergebnis in der Room-Datenbank.
     * Respektiert das Free-Tier-Limit: bei 3 Einträgen wird stattdessen
     * [SaveResult.LimitReached] emittiert, damit der Screen die Paywall zeigen kann.
     */
    fun save() {
        val current = _result.value ?: return
        if (_isSaved.value || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null

            try {
                val isPremium = premiumStatus.value is PremiumStatus.Premium

                if (!isPremium) {
                    val count = repository.getCount()
                    if (count >= FREE_SAVE_LIMIT) {
                        // Paywall-Signal: isSaved bleibt false, saveError trägt
                        // einen speziellen Marker damit der Screen die Paywall öffnet.
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

    /** Fehler-State zurücksetzen (z.B. nach Snackbar-Dismiss). */
    fun clearError() {
        _saveError.value = null
    }

    companion object {
        /**
         * Wenn saveError diesen Wert enthält, soll der Screen
         * die Upgrade-Paywall öffnen statt eine Fehlermeldung zu zeigen.
         */
        const val PAYWALL_TRIGGER = "PAYWALL"
    }
}