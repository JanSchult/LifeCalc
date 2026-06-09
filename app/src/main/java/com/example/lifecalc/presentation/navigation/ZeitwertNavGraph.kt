package com.example.lifecalc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifecalc.presentation.screens.history.HistoryScreen
import com.example.lifecalc.presentation.screens.input.InputScreen
import com.example.lifecalc.presentation.screens.results.ResultScreen



@Composable
fun ZeitwertNavGraph(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Input) {

        composable<Screen.Input> {
            InputScreen(
                onNavigateToResult  = { navController.navigate(Screen.Result) },
                onNavigateToHistory = { navController.navigate(Screen.History) }
            )
        }

        composable<Screen.Result> {
            ResultScreen(
                onBack    = { navController.popBackStack() },
                onHistory = { navController.navigate(Screen.History) }
            )
        }

        composable<Screen.History> {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}