// File: app/src/main/java/com/example/cs501_fp/viewmodel/AnalyticsViewModel.kt
// Computes financial statistics (Total Spent, Budget, Trends) based on the user's ticket history.

package com.example.cs501_fp.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class MonthlyStat(
    val monthLabel: String,
    val totalSpent: Double
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val prefs = UserPreferences(application)
    private val allEvents = database.userEventDao().getAllEvents()

    val monthlyBudget = prefs.monthlyBudget.stateIn(viewModelScope, SharingStarted.Lazily, 500.0)

    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonthSpending = allEvents.map { events ->
        val currentMonth = YearMonth.now()
        events.filter { event ->
            try {
                val date = LocalDate.parse(event.dateText)
                YearMonth.from(date) == currentMonth
            } catch (e: Exception) { false }
        }.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    @RequiresApi(Build.VERSION_CODES.O)
    val monthlyTrends = allEvents.map { events ->
        val current = YearMonth.now()
        val result = mutableListOf<MonthlyStat>()

        for (i in 5 downTo 0) {
            val targetMonth = current.minusMonths(i.toLong())

            val sum = events.filter { event ->
                try {
                    val date = LocalDate.parse(event.dateText)
                    YearMonth.from(date) == targetMonth
                } catch (e: Exception) { false }
            }.sumOf { it.price }

            result.add(MonthlyStat(
                monthLabel = targetMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                totalSpent = sum
            ))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lifetimeSpending = allEvents.map { events ->
        events.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val averageTicketPrice = allEvents.map { events ->
        if (events.isEmpty()) 0.0 else events.map { it.price }.average()
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            prefs.setMonthlyBudget(newBudget)
        }
    }
}
