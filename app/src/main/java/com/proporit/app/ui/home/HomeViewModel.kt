package com.proporit.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proporit.app.data.ReminderRepository
import com.proporit.app.data.ReminderSettings
import com.proporit.app.ui.components.RingStatus
import com.proporit.app.ui.util.dayWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val mainLabel: String = "…",
    val subLabel: String = "",
    val progress: Float = 0f,
    val status: RingStatus = RingStatus.OK,
    val lastChangedText: String = "—",
    val nextDueText: String = "—",
    val streak: Int = 0,
    val justMarked: Boolean = false,
)

class HomeViewModel(private val repo: ReminderRepository) : ViewModel() {

    private fun ticker(periodMs: Long) = flow {
        while (true) {
            emit(Unit)
            delay(periodMs)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(repo.settingsFlow, ticker(30_000)) { s, _ -> buildState(s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch { repo.rescheduleFromCurrentState() }
    }

    fun onMarkChanged() {
        viewModelScope.launch { repo.markChanged() }
    }

    private fun buildState(s: ReminderSettings): HomeUiState {
        val now = System.currentTimeMillis()
        val due = repo.nextDueMillis(s)
        val remaining = due - now
        val dayMs = TimeUnit.DAYS.toMillis(1)

        val overdue = remaining <= 0
        val daysLeftCeil = if (overdue) 0 else ((remaining + dayMs - 1) / dayMs).toInt()

        val status = when {
            overdue -> RingStatus.OVERDUE
            daysLeftCeil <= 1 -> RingStatus.WARNING
            else -> RingStatus.OK
        }

        val progress = if (s.intervalDays <= 0) 0f else {
            val elapsed = now - s.lastChangedEpochMillis
            val total = TimeUnit.DAYS.toMillis(s.intervalDays.toLong())
            (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }

        val mainLabel = if (overdue) "!" else daysLeftCeil.toString()
        val subLabel = if (overdue) {
            "заменить сейчас"
        } else {
            val verb = if (daysLeftCeil == 1) "остался" else "осталось"
            "${dayWord(daysLeftCeil)} $verb"
        }

        val sdf = SimpleDateFormat("dd.MM, HH:mm", Locale("ru"))
        val lastChangedText = if (s.lastChangedEpochMillis == 0L) "ещё не менял" else sdf.format(Date(s.lastChangedEpochMillis))
        val nextDueText = sdf.format(Date(due))

        return HomeUiState(
            mainLabel = mainLabel,
            subLabel = subLabel,
            progress = progress,
            status = status,
            lastChangedText = lastChangedText,
            nextDueText = nextDueText,
            streak = s.streak,
        )
    }

}
