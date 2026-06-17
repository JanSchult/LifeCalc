package com.example.lifecalc.Viewmodel.UiState

sealed class ExportState {
    object Idle : ExportState()
    object InProgress : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}