package com.example.lifecalc.data.preference


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_INCOME        = stringPreferencesKey("income")
        val KEY_IS_MONTHLY    = booleanPreferencesKey("is_monthly")
        val KEY_HOURS_PER_WEEK = stringPreferencesKey("hours_per_week")
        val KEY_TAX_PERCENT   = stringPreferencesKey("tax_percent")
    }
    val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_ONBOARDING_DONE] ?: false }

    val incomeFlow: Flow<String> = context.dataStore.data
        .map { it[KEY_INCOME] ?: "" }

    val isMonthlyFlow: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_IS_MONTHLY] ?: true }

    val hoursPerWeekFlow: Flow<String> = context.dataStore.data
        .map { it[KEY_HOURS_PER_WEEK] ?: "40" }

    val taxPercentFlow: Flow<String> = context.dataStore.data
        .map { it[KEY_TAX_PERCENT] ?: "30" }

    suspend fun saveIncome(value: String) {
        context.dataStore.edit { it[KEY_INCOME] = value }
    }
    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    suspend fun saveIsMonthly(value: Boolean) {
        context.dataStore.edit { it[KEY_IS_MONTHLY] = value }
    }

    suspend fun saveHoursPerWeek(value: String) {
        context.dataStore.edit { it[KEY_HOURS_PER_WEEK] = value }
    }

    suspend fun saveTaxPercent(value: String) {
        context.dataStore.edit { it[KEY_TAX_PERCENT] = value }
    }
}