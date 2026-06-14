package com.example.lifecalc.Viewmodel.UiState

import com.example.lifecalc.Viewmodel.ExportState
import com.example.lifecalc.domain.model.CalculationResult

data class HistoryUiState(
    val entries: List<CalculationResult> = emptyList(),
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val showDeleteConfirm: CalculationResult? = null,  // Entry, das gelöscht werden soll
    val showPaywall: Boolean = false,
    val exportState: ExportState = ExportState.Idle
)