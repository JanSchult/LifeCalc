package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.R
import com.example.lifecalc.ui.theme.Accent
import com.example.lifecalc.ui.theme.Danger
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface
import com.example.lifecalc.ui.theme.SurfaceAlt

@Composable
 fun BudgetSummaryCard(
    netIncome: Double,
    totalExpenses: Double,
    remaining: Double,
    remainingInHours: Double
) {
    val remainingColor = if (remaining >= 0) Accent else Danger

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Einnahmen / Ausgaben
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label =  stringResource(R.string.budget_net_income),
                    value = "+ %.2f €".format(netIncome),
                    valueColor = Accent
                )
                SummaryItem(
                    label =  stringResource(R.string.budget_expenses),
                    value = "- %.2f €".format(totalExpenses),
                    valueColor = Danger,
                    align = Alignment.End
                )
            }

            HorizontalDivider(color = SurfaceAlt)

            // Verbleibend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text( stringResource(R.string.budget_expenses), fontSize = 12.sp, color = OnSurface)
                    Text(
                        text = "%.2f €".format(remaining),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = remainingColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text( stringResource(R.string.budget_equals), fontSize = 11.sp, color = OnSurface)
                    Text(
                        text = "%.1f Std.".format(remainingInHours),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text( stringResource(R.string.budget_lifetime), fontSize = 11.sp, color = OnSurface)
                }
            }

            // Fortschrittsbalken: Ausgaben / Einkommen
            if (netIncome > 0) {
                val ratio = (totalExpenses / netIncome).coerceIn(0.0, 1.0).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text( stringResource(R.string.budget_consumed), fontSize = 11.sp, color = OnSurface)
                        Text(
                            "%.0f%%".format(ratio * 100),
                            fontSize = 11.sp,
                            color = if (ratio > 0.9f) Danger else OnSurface
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (ratio > 0.9f) Danger else Primary,
                        trackColor = SurfaceAlt
                    )
                }
            }
        }
    }
}