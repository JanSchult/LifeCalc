package com.example.lifecalc.Viewmodel

import androidx.lifecycle.ViewModel
import com.example.lifecalc.domain.model.CalculationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _result = MutableStateFlow<CalculationResult?>(null)
    val result: StateFlow<CalculationResult?> = _result.asStateFlow()

    fun setResult(result: CalculationResult) {
        _result.value = result
    }
}