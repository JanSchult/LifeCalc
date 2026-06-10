package com.example.lifecalc.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifecalc.Viewmodel.SharedViewModel
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.presentation.screens.BugetScreen.BudgetScreen
import com.example.lifecalc.presentation.screens.history.HistoryScreen
import com.example.lifecalc.presentation.screens.input.InputScreen
import com.example.lifecalc.presentation.screens.onboarding.OnboardingScreen
import com.example.lifecalc.presentation.screens.results.ResultScreen
import com.example.lifecalc.ui.theme.Background
import org.koin.androidx.compose.koinViewModel


@Composable
fun ZeitwertNavGraph(modifier: Modifier) {
    val navController = rememberNavController()

    val sharedViewModel: SharedViewModel = koinViewModel()
    // Onboarding-Status aus Preferences lesen
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val onboardingDone by userPreferences.onboardingDoneFlow
        .collectAsState(initial = null)  // null = noch nicht geladen

    // Solange Status nicht geladen → nichts rendern (verhindert falschen Start)
    if (onboardingDone == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        )
        return
    }

    NavHost(
        navController = navController,
        // Direkt zu Input wenn Onboarding bereits erledigt
        startDestination = if (onboardingDone == true)
            Screen.Input.route
        else
            Screen.Onboarding.route,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Input.route) {
                        // Onboarding aus dem Backstack entfernen
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Input.route) {
            InputScreen(
                sharedViewModel= sharedViewModel,
                onNavigateToResult = { navController.navigate(Screen.Result.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                sharedViewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
                onHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Budget.route) {
            BudgetScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}