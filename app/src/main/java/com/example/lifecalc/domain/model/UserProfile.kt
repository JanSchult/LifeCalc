package com.example.lifecalc.domain.model

data class UserProfile(
    val hourlyRateNet: Double,        // Nettostundenlohn (nach Abzügen)
    val hoursPerWeek: Double,         // Arbeitsstunden/Woche
    val taxDeductionPercent: Double   // Steuer/Abzüge in %
) {
    companion object {
        fun fromMonthlyGross(
            monthlyGross: Double,
            hoursPerWeek: Double,
            taxPercent: Double
        ): UserProfile {
            val net = monthlyGross * (1 - taxPercent / 100)
            val monthlyHours = (hoursPerWeek / 5) * (52.0 / 12) * 5
            return UserProfile(
                hourlyRateNet = net / monthlyHours,
                hoursPerWeek = hoursPerWeek,
                taxDeductionPercent = taxPercent
            )
        }
    }
}