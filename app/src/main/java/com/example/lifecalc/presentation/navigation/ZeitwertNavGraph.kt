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

    NavHost(navController = navController, startDestination = Screen.Input.route) {

        composable(Screen.Input.route) {
            InputScreen(
                onNavigateToResult = { navController.navigate(Screen.Result.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                onBack = { navController.popBackStack() },
                onHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}