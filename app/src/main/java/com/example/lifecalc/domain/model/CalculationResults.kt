package com.example.lifecalc.domain.model


import java.time.Instant

data class CalculationResult(
    val id: Long = 0,
    val targetAmount: Double,
    val targetLabel: String,          // z.B. "iPhone 16 Pro"
    val hourlyRateNet: Double,
    val hoursRequired: Double,
    val daysRequired: Double,
    val weeksRequired: Double,
    val monthsRequired: Double,
    val lifePercentage: Double,        // % der jährlichen Arbeitszeit
    val emotionalMessage: String,
    val timestamp: Instant = Instant.now()
) {
    val formattedHours: String get() = "%.1f Stunden".format(hoursRequired)
    val formattedDays: String get() = "%.1f Tage".format(daysRequired)
    val formattedWeeks: String get() = "%.1f Wochen".format(weeksRequired)
    val formattedMonths: String get() = "%.2f Monate".format(monthsRequired)
}