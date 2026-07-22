package com.proporit.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proporit.app.data.ReminderRepository
import com.proporit.app.data.ReminderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: ReminderRepository) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings())

    fun setIntervalDays(days: Int) = viewModelScope.launch { repo.updateIntervalDays(days.coerceIn(1, 14)) }
    fun setFrequencyHours(hours: Int) = viewModelScope.launch { repo.updateFrequencyHours(hours.coerceIn(1, 12)) }
    fun setSound(enabled: Boolean) = viewModelScope.launch { repo.updateSoundEnabled(enabled) }
    fun setVibration(enabled: Boolean) = viewModelScope.launch { repo.updateVibrationEnabled(enabled) }
    fun setQuietHours(enabled: Boolean, start: Int, end: Int) = viewModelScope.launch {
        repo.updateQuietHours(enabled, start.coerceIn(0, 23), end.coerceIn(0, 23))
    }
}
