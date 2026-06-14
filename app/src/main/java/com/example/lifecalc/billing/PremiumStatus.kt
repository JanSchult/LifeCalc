package com.example.lifecalc.billing

sealed class PremiumStatus {
    object Loading  : PremiumStatus()
    object Free     : PremiumStatus()
    object Premium  : PremiumStatus()
}