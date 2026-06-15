package com.example.lifecalc.domain.model

import android.content.Context
import com.example.lifecalc.R

enum class ExpenseCategory(val stringRes: Int, val emoji: String) {
    WOHNEN(R.string.category_wohnen, "🏠"),
    TRANSPORT(R.string.category_transport, "🚗"),
    LEBENSMITTEL(R.string.category_lebensmittel, "🛒"),
    UNTERHALTUNG(R.string.category_unterhaltung, "🎬"),
    GESUNDHEIT(R.string.category_gesundheit, "💊"),
    VERSICHERUNG(R.string.category_versicherung, "🛡️"),
    ABONNEMENTS(R.string.category_abonnements, "📱"),
    SONSTIGES(R.string.category_sonstiges, "📦");

    fun getLabel(context: Context): String = context.getString(stringRes)
}