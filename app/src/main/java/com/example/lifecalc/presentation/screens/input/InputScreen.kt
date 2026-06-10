package com.example.lifecalc.presentation.screens.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.Viewmodel.InputViewModel
import com.example.lifecalc.Viewmodel.SharedViewModel
import com.example.lifecalc.presentation.screens.composables.SectionLabel
import com.example.lifecalc.presentation.screens.composables.ZeitwertTextField
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.Danger
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.SurfaceAlt
import org.koin.androidx.compose.koinViewModel

@Composable
fun InputScreen(
    sharedViewModel: SharedViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBudget: () -> Unit,
    viewModel: InputViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.navigateToResult) {
        if (state.navigateToResult) {
            viewModel.resetNavigation()
            onNavigateToResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Text(
                text = "ZEITWERT",
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Was kostet\ndein Leben?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                lineHeight = 38.sp
            )
        }

        HorizontalDivider(thickness = 1.dp, color = SurfaceAlt)

        // Einkommens-Sektion
        SectionLabel("DEIN EINKOMMEN")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZeitwertTextField(
                value = state.incomeInput,
                onValueChange = viewModel::onIncomeChange,
                label = if (state.isMonthly) "Bruttogehalt (€)" else "Bruttostundenlohn (€)",
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = state.isMonthly,
                onClick = viewModel::onToggleIncomeType,
                label = { Text(if (state.isMonthly) "Monat" else "Stunde", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Background
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ZeitwertTextField(
                value = state.hoursPerWeek,
                onValueChange = viewModel::onHoursChange,
                label = "Stunden/Woche",
                modifier = Modifier.weight(1f)
            )
            ZeitwertTextField(
                value = state.taxPercent,
                onValueChange = viewModel::onTaxChange,
                label = "Abzüge (%)",
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = SurfaceAlt)

        // Ziel-Sektion
        SectionLabel("WOFÜR ARBEITEST DU?")

        ZeitwertTextField(
            value = state.targetLabel,
            onValueChange = viewModel::onTargetLabelChange,
            label = "Bezeichnung (z.B. iPhone, Urlaub...)",
            keyboardType = KeyboardType.Text
        )
        ZeitwertTextField(
            value = state.targetAmount,
            onValueChange = viewModel::onTargetAmountChange,
            label = "Betrag in €"
        )

        // Fehlerhinweis
        AnimatedVisibility(visible = state.error != null) {
            Text(
                text = state.error ?: "",
                color = Danger,
                fontSize = 14.sp
            )
        }

        // CTA-Button
        Button(
            onClick = { viewModel.calculate(sharedViewModel) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Background
            ),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text(
                "JETZT BERECHNEN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // History-Link
        TextButton(
            onClick = onNavigateToHistory,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Frühere Berechnungen", color = OnSurface, fontSize = 13.sp)
        }
        TextButton(
            onClick = onNavigateToBudget,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("💰  Budget-Übersicht", color = OnSurface, fontSize = 13.sp)
        }
    }
}
