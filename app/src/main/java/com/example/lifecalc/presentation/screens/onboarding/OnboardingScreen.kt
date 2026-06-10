package com.example.lifecalc.presentation.screens.onboarding


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.Viewmodel.OnboardingViewModel
import com.example.lifecalc.presentation.screens.composables.SectionLabel
import com.example.lifecalc.presentation.screens.composables.ZeitwertTextField
import com.example.lifecalc.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        // ── Hero ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏳",
                fontSize = 56.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "ZEITWERT",
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Deine Zeit\nhat einen Preis.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Bevor du einen Kauf machst — sieh was er\nwirklich kostet. In Stunden deines Lebens.",
                fontSize = 15.sp,
                color = OnSurface,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(color = SurfaceAlt)

        // ── Eingabe ───────────────────────────────────────────────────────
        SectionLabel("WIE VIEL VERDIENST DU?")

        Text(
            text = "Nur du siehst diese Daten — sie bleiben auf deinem Gerät.",
            fontSize = 12.sp,
            color = PrimaryDim
        )

        // Einkommen + Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZeitwertTextField(
                value = state.incomeInput,
                onValueChange = viewModel::onIncomeChange,
                label = if (state.isMonthly) "Bruttogehalt (€)" else "Stundenlohn (€)",
                modifier = Modifier.weight(1f),
                isError = state.incomeError != null,
                errorMessage = state.incomeError
            )
            FilterChip(
                selected = state.isMonthly,
                onClick = viewModel::onToggleIncomeType,
                label = {
                    Text(
                        if (state.isMonthly) "Monat" else "Stunde",
                        fontSize = 12.sp
                    )
                },
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
                modifier = Modifier.weight(1f),
                isError = state.hoursError != null,
                errorMessage = state.hoursError
            )
            ZeitwertTextField(
                value = state.taxPercent,
                onValueChange = viewModel::onTaxChange,
                label = "Abzüge (%)",
                modifier = Modifier.weight(1f),
                isError = state.taxError != null,
                errorMessage = state.taxError
            )
        }

        // ── CTA ───────────────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.completeOnboarding(onComplete) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Background
            ),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Background
                )
            } else {
                Text(
                    "LOSLEGEN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Text(
            text = "Du kannst alle Angaben jederzeit ändern.",
            fontSize = 12.sp,
            color = OnSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}