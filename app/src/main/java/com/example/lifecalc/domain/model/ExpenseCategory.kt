package com.example.lifecalc.domain.model

enum class ExpenseCategory(val label: String, val emoji: String) {
    WOHNEN("Wohnen", "🏠"),
    TRANSPORT("Transport", "🚗"),
    LEBENSMITTEL("Lebensmittel", "🛒"),
    UNTERHALTUNG("Unterhaltung", "🎬"),
    GESUNDHEIT("Gesundheit", "💊"),
    VERSICHERUNG("Versicherung", "🛡️"),
    ABONNEMENTS("Abos", "📱"),
    SONSTIGES("Sonstiges", "📦")
}