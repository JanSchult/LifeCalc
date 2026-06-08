package com.example.lifecalc.domain.usecase

import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.domain.model.UserProfile
import kotlin.math.roundToInt

class CalculateLifetimeUseCase {

    operator fun invoke(
        profile: UserProfile,
        targetAmount: Double,
        targetLabel: String
    ): CalculationResult {
        require(profile.hourlyRateNet > 0) { "Stundenlohn muss > 0 sein" }
        require(targetAmount >= 0) { "Betrag muss >= 0 sein" }

        val hoursRequired = targetAmount / profile.hourlyRateNet
        val daysRequired = hoursRequired / (profile.hoursPerWeek / 5.0)
        val weeksRequired = hoursRequired / profile.hoursPerWeek
        val monthsRequired = weeksRequired / 4.33

        val yearlyHours = profile.hoursPerWeek * 52
        val lifePercentage = (hoursRequired / yearlyHours) * 100

        return CalculationResult(
            targetAmount = targetAmount,
            targetLabel = targetLabel.ifBlank { "%.2f €".format(targetAmount) },
            hourlyRateNet = profile.hourlyRateNet,
            hoursRequired = hoursRequired,
            daysRequired = daysRequired,
            weeksRequired = weeksRequired,
            monthsRequired = monthsRequired,
            lifePercentage = lifePercentage,
            emotionalMessage = generateMessage(hoursRequired, targetAmount, targetLabel)
        )
    }

    private fun generateMessage(hours: Double, amount: Double, label: String): String {
        val name = label.ifBlank { "diesen Kauf" }
        return when {
            hours < 1    -> "Weniger als eine Stunde deines Lebens. Schnell verdient."
            hours < 4    -> "Einen halben Arbeitstag — für $name."
            hours < 8    -> "Ein voller Arbeitstag deines Lebens."
            hours < 24   -> "${hours.roundToInt()} Stunden. Fast drei Tage, an denen du morgens aufgestanden bist."
            hours < 80   -> "Zwei Wochen. Zeit, die du nie zurückbekommst."
            hours < 160  -> "Einen ganzen Arbeitsmonat. Hast du diesen Monat gut gelebt?"
            hours < 520  -> "Mehrere Monate deines Lebens. Ist $name das wert?"
            else         -> "Mehr als ein Jahr Lebenszeit. Das ist keine Zahl mehr — das ist ein Lebensabschnitt."
        }
    }
}