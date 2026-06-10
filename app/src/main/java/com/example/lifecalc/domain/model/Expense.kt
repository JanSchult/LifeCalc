package com.example.lifecalc.domain.model


data class Expense(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: ExpenseCategory
)