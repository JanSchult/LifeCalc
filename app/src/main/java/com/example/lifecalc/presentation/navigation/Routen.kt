package com.example.lifecalc.presentation.navigation

sealed class Screen(val route: String) {

    object Onboarding : Screen("onboarding")

    object Input   : Screen("input")
    object Result  : Screen("result")
    object History : Screen("history")

    object Budget  : Screen("budget")

}