package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.domain.model.CalculationResult
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.PrimaryDim
import com.example.lifecalc.ui.theme.Surface
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryItem(result: CalculationResult) {
    val formatter = DateTimeFormatter.ofPattern("dd. MMM, HH:mm")
        .withZone(ZoneId.systemDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(result.targetLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                Text("%.2f €".format(result.targetAmount), fontSize = 12.sp, color = OnSurface)
                Text(formatter.format(result.timestamp), fontSize = 11.sp, color = PrimaryDim)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(result.formattedHours, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text("Arbeitszeit", fontSize = 11.sp, color = OnSurface)
            }
        }
    }
}

