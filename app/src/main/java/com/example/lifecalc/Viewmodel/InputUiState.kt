package com.example.lifecalc.Viewmodel

import com.example.lifecalc.domain.model.CalculationResult

data class InputUiState(
    val incomeInput: String = "",
    val isMonthly: Boolean = true,
    val hoursPerWeek: String = "40",
    val taxPercent: String = "30",
    val targetAmount: String = "",
    val targetLabel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: CalculationResult? = null,
    val navigateToResult: Boolean = false
)