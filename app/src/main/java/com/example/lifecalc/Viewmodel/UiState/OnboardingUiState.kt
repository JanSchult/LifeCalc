package com.example.lifecalc.Viewmodel.UiState

data class OnboardingUiState(
    val incomeInput: String = "",
    val isMonthly: Boolean = true,
    val hoursPerWeek: String = "40",
    val taxPercent: String = "30",
    val incomeError: String? = null,
    val hoursError: String? = null,
    val taxError: String? = null,
    val isLoading: Boolean = false
)