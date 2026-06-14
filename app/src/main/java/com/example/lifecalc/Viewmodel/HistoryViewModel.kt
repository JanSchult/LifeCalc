package com.example.lifecalc.Viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecalc.Viewmodel.UiState.HistoryUiState
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.billing.PremiumStatus
import com.example.lifecalc.data.repository.CalculationRepository
import com.example.lifecalc.domain.model.CalculationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



sealed class ExportState {
    object Idle : ExportState()
    object InProgress : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

class HistoryViewModel(
    private val repository: CalculationRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    // ── Premium-Status ────────────────────────────────────────────────────

    val premiumStatus: StateFlow<PremiumStatus> = billingManager.premiumStatus

    private val isPremiumFlow = billingManager.premiumStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, PremiumStatus.Loading)

    // ── UI-State ──────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /**
     * Kombinierter History-Flow:
     * Reagiert automatisch auf Änderungen in DB UND Premium-Status.
     * Free: max. 3 Einträge | Premium: alle
     */
    val history: StateFlow<List<CalculationResult>> = combine(
        isPremiumFlow,
        // Wir fragen immer alle ab und schneiden im combine — so bleibt
        // der Flow reaktiv wenn sich Premium-Status ändert (Kauf in Session)
        repository.getHistory(isPremium = true)
    ) { status, allEntries ->
        val isPremium = status is PremiumStatus.Premium
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isPremium = isPremium
        )
        if (isPremium) allEntries else allEntries.take(3)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // ── Delete ────────────────────────────────────────────────────────────

    /** Zeigt Bestätigungs-Dialog für das zu löschende Item. */
    fun requestDelete(entry: CalculationResult) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = entry)
    }

    /** Nutzer hat Delete im Dialog bestätigt. */
    fun confirmDelete() {
        val entry = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            repository.delete(entry)
            _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
        }
    }

    /** Nutzer hat Delete-Dialog abgebrochen. */
    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
    }

    /** Löscht die gesamte Historie (Premium-Funktion). */
    fun deleteAll() {
        if (!(_uiState.value.isPremium)) {
            _uiState.value = _uiState.value.copy(showPaywall = true)
            return
        }
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // ── Export (Premium) ──────────────────────────────────────────────────

    /**
     * Exportiert die Historie als CSV.
     * Nur für Premium-Nutzer — sonst wird die Paywall gezeigt.
     * Die eigentliche File-Logik liegt im UseCase (hier als Stub für MVP).
     */
    fun exportCsv() {
        if (!_uiState.value.isPremium) {
            _uiState.value = _uiState.value.copy(showPaywall = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(exportState = ExportState.InProgress)
            try {
                val csv = buildCsvContent(history.value)
                // In echtem Code: FileRepository.writeCsv(csv) → gibt Pfad zurück
                // Hier Stub für MVP:
                val fakePath = "/storage/emulated/0/Downloads/zeitwert_export.csv"
                _uiState.value = _uiState.value.copy(
                    exportState = ExportState.Success(fakePath)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    exportState = ExportState.Error(e.localizedMessage ?: "Export fehlgeschlagen")
                )
            }
        }
    }

    private fun buildCsvContent(entries: List<CalculationResult>): String {
        val header = "Bezeichnung,Betrag (€),Stunden,Tage,Wochen,Monate,Datum\n"
        val rows = entries.joinToString("\n") { e ->
            "\"${e.targetLabel}\",${e.targetAmount},${e.hoursRequired}," +
                    "${e.daysRequired},${e.weeksRequired},${e.monthsRequired},${e.timestamp}"
        }
        return header + rows
    }

    fun clearExportState() {
        _uiState.value = _uiState.value.copy(exportState = ExportState.Idle)
    }

    // ── Paywall / Billing ─────────────────────────────────────────────────

    fun showPaywall() {
        _uiState.value = _uiState.value.copy(showPaywall = true)
    }

    fun dismissPaywall() {
        _uiState.value = _uiState.value.copy(showPaywall = false)
    }

    /**
     * Startet den Google Play Billing Flow für das Monats-Abo.
     * Activity-Referenz nötig — wird aus dem Composable übergeben.
     */
    fun launchPremiumMonthly(activity: Activity) {
        billingManager.launchMonthlySubscription(activity)
        dismissPaywall()
    }

    fun launchPremiumLifetime(activity: Activity) {
        billingManager.launchYearlySubscription(activity)
        dismissPaywall()
    }

    // Legacy-Alias für HistoryScreen (kein Activity-Kontext nötig für einfache Paywall-Anzeige)
    fun launchPremium() {
        showPaywall()
    }

    // ── Koin-DI: deleteAll braucht auch die Repository-Methode ────────────

    // Sicherheitshalber — falls Repository noch keine deleteAll hat:
    private suspend fun CalculationRepository.deleteAll() {
        // Muss in CalculationRepository ergänzt werden:
        // suspend fun deleteAll() = dao.deleteAll()
        // Hier als Reminder-Kommentar im ViewModel
    }
}