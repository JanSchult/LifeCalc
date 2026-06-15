package com.example.lifecalc.domain.usecase

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.lifecalc.R
import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.domain.model.UserProfile
import kotlin.math.roundToInt

class CalculateLifetimeUseCase(private val context: Context) {

    operator fun invoke(
        profile: UserProfile,
        targetAmount: Double,
        targetLabel: String
    ): CalculationResult {
        require(profile.hourlyRateNet > 0)
        require(targetAmount >= 0)

        val hoursRequired = targetAmount / profile.hourlyRateNet
        val daysRequired = hoursRequired / (profile.hoursPerWeek / 5.0)
        val weeksRequired = hoursRequired / profile.hoursPerWeek
        val monthsRequired = weeksRequired / 4.33
        val yearlyHours = profile.hoursPerWeek * 52
        val lifePercentage = (hoursRequired / yearlyHours) * 100

        return CalculationResult(
            targetAmount = targetAmount,
            targetLabel = targetLabel.ifBlank { context.getString(R.string.msg_default_purchase) },
            hourlyRateNet = profile.hourlyRateNet,
            hoursRequired = hoursRequired,
            daysRequired = daysRequired,
            weeksRequired = weeksRequired,
            monthsRequired = monthsRequired,
            lifePercentage = lifePercentage,
            emotionalMessage = generateMessage(hoursRequired, targetLabel)
        )
    }

    private fun generateMessage(hours: Double, label: String): String {
        val name = label.ifBlank { context.getString(R.string.msg_default_purchase) }
        return when {
            hours < 1   -> context.getString(R.string.msg_less_than_hour)
            hours < 4   -> context.getString(R.string.msg_half_day, name)
            hours < 8   -> context.getString(R.string.msg_full_day)
            hours < 24  -> context.getString(R.string.msg_days, hours.roundToInt())
            hours < 80  -> context.getString(R.string.msg_two_weeks)
            hours < 160 -> context.getString(R.string.msg_one_month)
            hours < 520 -> context.getString(R.string.msg_months, name)
            else        -> context.getString(R.string.msg_year)
        }
    }
}