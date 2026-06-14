package com.example.lifecalc.presentation.screens.BugetScreen

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.Viewmodel.BudgetViewModel
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.presentation.screens.composables.BudgetSummaryCard
import com.example.lifecalc.presentation.screens.composables.ExpenseDialog
import com.example.lifecalc.presentation.screens.composables.ExpenseItem
import com.example.lifecalc.presentation.screens.composables.PaywallDialog
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.PrimaryDim
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@SuppressLint("ContextCastToActivity")
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val billingManager: BillingManager = koinInject()
    val activity = LocalContext.current as Activity
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp,
                top = 48.dp, bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "BUDGET",
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Was bleibt\nvon deinem Leben?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    lineHeight = 34.sp
                )
            }

            // Übersichts-Karte
            item {
                BudgetSummaryCard(
                    netIncome = state.netIncome,
                    totalExpenses = state.totalExpenses,
                    remaining = state.remaining,
                    remainingInHours = state.remainingInHours
                )
            }

            // Ausgaben-Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONATLICHE AUSGABEN",
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        color = PrimaryDim,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.expenses.size} Einträge",
                        fontSize = 12.sp,
                        color = OnSurface
                    )
                }
            }

            // Ausgaben-Liste
            if (state.expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Noch keine Ausgaben eingetragen",
                            color = OnSurface,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(state.expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = { viewModel.openEditDialog(expense) },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.openAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Background
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ausgabe hinzufügen")
        }
    }

    // Add/Edit Dialog
    if (state.showAddDialog) {
        ExpenseDialog(
            isEditing = state.editingExpense != null,
            name = state.dialogName,
            amount = state.dialogAmount,
            category = state.dialogCategory,
            onNameChange = viewModel::onDialogNameChange,
            onAmountChange = viewModel::onDialogAmountChange,
            onCategoryChange = viewModel::onDialogCategoryChange,
            onConfirm = { viewModel.saveExpense() },
            onDismiss = { viewModel.closeDialog() }
        )
    }

    if (state.showPaywall) {
        PaywallDialog(
            onDismiss = { viewModel.dismissPaywall() },
            onMonthly = {
                billingManager.launchMonthlySubscription(activity)
                viewModel.dismissPaywall()
            },
            onYearly = {
                billingManager.launchYearlySubscription(activity)
                viewModel.dismissPaywall()
            }
        )
    }
}