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
fun ZeitwertNavGraph(
    startDestination: String,   // ← neu
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,  // ← direkt verwenden
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Input.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Input.route) {
            InputScreen(
                sharedViewModel = sharedViewModel,
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
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Budget.route) {
            BudgetScreen(onBack = { navController.popBackStack() })
        }
    }
}