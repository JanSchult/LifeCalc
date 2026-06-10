package com.example.lifecalc.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifecalc.domain.model.ExpenseEntity

@Database(entities = [CalculationEntity::class, ExpenseEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun expenseDao(): ExpenseDao
}