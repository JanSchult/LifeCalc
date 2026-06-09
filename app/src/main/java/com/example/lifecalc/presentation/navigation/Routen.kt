package com.example.lifecalc.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Input : Screen()

    @Serializable
    data object Result : Screen()

    @Serializable
    data object History : Screen()
}