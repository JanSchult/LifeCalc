package com.example.lifecalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetAmount: Double,
    val targetLabel: String,
    val hoursRequired: Double,
    val daysRequired: Double,
    val weeksRequired: Double,
    val monthsRequired: Double,
    val lifePercentage: Double,
    val emotionalMessage: String,
    val timestampMillis: Long = System.currentTimeMillis()
)