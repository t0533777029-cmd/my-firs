package com.proporit.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proporit.app.data.ChangeEvent
import com.proporit.app.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HistoryRow(val dateText: String, val timeText: String, val onTime: Boolean)

data class CalendarUiState(
    val monthLabel: String = "",
    val leadingBlanks: Int = 0,
    val daysInMonth: Int = 30,
    val doneDays: Set<Int> = emptySet(),
    val dueDay: Int? = null,
    val today: Int = 1,
    val history: List<HistoryRow> = emptyList(),
)

class CalendarViewModel(private val repo: ReminderRepository) : ViewModel() {

    val uiState: StateFlow<CalendarUiState> = combine(repo.settingsFlow, repo.historyFlow) { settings, events ->
        buildState(settings.let { repo.nextDueMillis(it) }, events)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    private fun buildState(dueMillis: Long, events: List<ChangeEvent>): CalendarUiState {
        val now = Calendar.getInstance()
        val monthFmt = SimpleDateFormat("LLLL yyyy", Locale("ru"))
        val monthLabel = monthFmt.format(now.time).replaceFirstChar { it.uppercase() }

        val first = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        // Convert Sunday-first (Calendar.DAY_OF_WEEK: Sun=1..Sat=7) to Monday-first index 0..6
        val rawDow = first.get(Calendar.DAY_OF_WEEK)
        val leadingBlanks = ((rawDow + 5) % 7)
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = now.get(Calendar.DAY_OF_MONTH)

        val monthStart = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val monthEnd = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, daysInMonth); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        }.timeInMillis

        val doneDays = events
            .filter { it.timestampEpochMillis in monthStart..monthEnd }
            .map { Calendar.getInstance().apply { timeInMillis = it.timestampEpochMillis }.get(Calendar.DAY_OF_MONTH) }
            .toSet()

        val dueDay = if (dueMillis in monthStart..monthEnd) {
            Calendar.getInstance().apply { timeInMillis = dueMillis }.get(Calendar.DAY_OF_MONTH)
        } else null

        val historySdfDate = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
        val historySdfTime = SimpleDateFormat("HH:mm", Locale("ru"))
        val history = events.take(10).map {
            HistoryRow(
                dateText = historySdfDate.format(Date(it.timestampEpochMillis)).replaceFirstChar { c -> c.uppercase() },
                timeText = historySdfTime.format(Date(it.timestampEpochMillis)),
                onTime = it.onTime,
            )
        }

        return CalendarUiState(
            monthLabel = monthLabel,
            leadingBlanks = leadingBlanks,
            daysInMonth = daysInMonth,
            doneDays = doneDays,
            dueDay = dueDay,
            today = today,
            history = history,
        )
    }
}
