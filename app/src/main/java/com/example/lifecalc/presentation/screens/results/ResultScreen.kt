package com.example.lifecalc.presentation.screens.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.Viewmodel.ResultViewModel
import com.example.lifecalc.Viewmodel.SharedViewModel
import com.example.lifecalc.presentation.screens.composables.LifetimeBar
import com.example.lifecalc.presentation.screens.composables.MetricCard
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.PrimaryDim
import com.example.lifecalc.ui.theme.Surface
import com.example.lifecalc.ui.theme.SurfaceAlt
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResultScreen(
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    viewModel: ResultViewModel = koinViewModel()
) {
    val result by sharedViewModel.result.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    result?.let { r ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Back-Button
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = OnSurface)
            }

            // Hauptaussage
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = r.targetLabel,
                    fontSize = 13.sp,
                    color = OnSurface,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "%.2f €".format(r.targetAmount),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "kostet dich",
                    fontSize = 14.sp,
                    color = OnSurface
                )
                Text(
                    text = r.formattedHours,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    text = "deines Lebens",
                    fontSize = 14.sp,
                    color = OnSurface
                )
            }

            // Lebenszeit-Balken
            LifetimeBar(percentage = r.lifePercentage.coerceAtMost(100.0).toFloat())

            // Metriken-Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Tage",    r.formattedDays,   Modifier.weight(1f))
                MetricCard("Wochen",  r.formattedWeeks,  Modifier.weight(1f))
                MetricCard("Monate",  r.formattedMonths, Modifier.weight(1f))
            }

            // Emotionale Botschaft
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceAlt),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "\"${r.emotionalMessage}\"",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 16.sp,
                    color = OnBackground,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Speichern-Button
            OutlinedButton(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaved,
                border = BorderStroke(1.dp, if (isSaved) PrimaryDim else Primary)
            ) {
                Text(
                    text = if (isSaved) "✓ Gespeichert" else "In Historie speichern",
                    color = if (isSaved) PrimaryDim else Primary
                )
            }

            // Neue Berechnung
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Surface,
                    contentColor = OnBackground
                )
            ) {
                Text("Neue Berechnung")
            }
        }
    }
}