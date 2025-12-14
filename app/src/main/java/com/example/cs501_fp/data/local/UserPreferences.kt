// File: app/src/main/java/com/example/cs501_fp/data/local/UserPreferences.kt
// Manages lightweight key-value storage for user settings like monthly budget.

package com.example.cs501_fp.data.local

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val MONTHLY_BUDGET_KEY = doublePreferencesKey("monthly_budget")
    }

    val monthlyBudget: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[MONTHLY_BUDGET_KEY] ?: 500.0
        }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET_KEY] = amount
        }
    }
}
