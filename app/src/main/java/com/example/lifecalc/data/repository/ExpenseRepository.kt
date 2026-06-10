package com.example.lifecalc.data.repository

import com.example.lifecalc.data.db.ExpenseDao
import com.example.lifecalc.domain.model.Expense
import com.example.lifecalc.domain.model.ExpenseCategory
import com.example.lifecalc.domain.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val dao: ExpenseDao) {

    fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    fun getTotalExpenses(): Flow<Double> =
        dao.getTotalExpenses().map { it ?: 0.0 }

    suspend fun getCount(): Int = dao.getCount()

    suspend fun insert(expense: Expense) =
        dao.insert(expense.toEntity())

    suspend fun update(expense: Expense) =
        dao.update(expense.toEntity())

    suspend fun delete(expense: Expense) =
        dao.delete(expense.toEntity())
}

// Mapper
private fun ExpenseEntity.toDomain() = Expense(
    id = id,
    name = name,
    amount = amount,
    category = ExpenseCategory.entries.find { it.name == category }
        ?: ExpenseCategory.SONSTIGES
)

private fun Expense.toEntity() = ExpenseEntity(
    id = id,
    name = name,
    amount = amount,
    category = category.name
)