package com.example.lifecalc.Viewmodel.UiState

data class InputUiState(
    val incomeInput: String = "",
    val isMonthly: Boolean = true,
    val hoursPerWeek: String = "40",
    val taxPercent: String = "30",
    val targetAmount: String = "",
    val targetLabel: String = "",
    val isLoading: Boolean = false,
    val navigateToResult: Boolean = false,

    // Globaler Fehler (Logikfehler die kein Feld betreffen)
    val error: String? = null,

    // Fehler pro Feld
    val incomeError: String? = null,
    val hoursError: String? = null,
    val taxError: String? = null,
    val targetAmountError: String? = null
) {
    val hasFieldErrors: Boolean
        get() = incomeError != null ||
                hoursError != null ||
                taxError != null ||
                targetAmountError != null
}