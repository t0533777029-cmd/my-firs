package com.proporit.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "proporit_settings")

data class ReminderSettings(
    val intervalDays: Int = 3,
    val frequencyHours: Int = 1,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietStartHour: Int = 23,
    val quietEndHour: Int = 7,
    val lastChangedEpochMillis: Long = 0L,
    val streak: Int = 0,
)

object PrefsKeys {
    val INTERVAL_DAYS = intPreferencesKey("interval_days")
    val FREQUENCY_HOURS = intPreferencesKey("frequency_hours")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
    val QUIET_START_HOUR = intPreferencesKey("quiet_start_hour")
    val QUIET_END_HOUR = intPreferencesKey("quiet_end_hour")
    val LAST_CHANGED = longPreferencesKey("last_changed_epoch")
    val STREAK = intPreferencesKey("streak")
}

class PrefsStore(private val context: Context) {

    val settingsFlow: Flow<ReminderSettings> = context.dataStore.data.map { p ->
        ReminderSettings(
            intervalDays = p[PrefsKeys.INTERVAL_DAYS] ?: 3,
            frequencyHours = p[PrefsKeys.FREQUENCY_HOURS] ?: 1,
            soundEnabled = p[PrefsKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = p[PrefsKeys.VIBRATION_ENABLED] ?: true,
            quietHoursEnabled = p[PrefsKeys.QUIET_HOURS_ENABLED] ?: false,
            quietStartHour = p[PrefsKeys.QUIET_START_HOUR] ?: 23,
            quietEndHour = p[PrefsKeys.QUIET_END_HOUR] ?: 7,
            lastChangedEpochMillis = p[PrefsKeys.LAST_CHANGED] ?: 0L,
            streak = p[PrefsKeys.STREAK] ?: 0,
        )
    }

    suspend fun setIntervalDays(days: Int) = context.dataStore.edit { it[PrefsKeys.INTERVAL_DAYS] = days }
    suspend fun setFrequencyHours(hours: Int) = context.dataStore.edit { it[PrefsKeys.FREQUENCY_HOURS] = hours }
    suspend fun setSoundEnabled(v: Boolean) = context.dataStore.edit { it[PrefsKeys.SOUND_ENABLED] = v }
    suspend fun setVibrationEnabled(v: Boolean) = context.dataStore.edit { it[PrefsKeys.VIBRATION_ENABLED] = v }
    suspend fun setQuietHoursEnabled(v: Boolean) = context.dataStore.edit { it[PrefsKeys.QUIET_HOURS_ENABLED] = v }
    suspend fun setQuietRange(start: Int, end: Int) = context.dataStore.edit {
        it[PrefsKeys.QUIET_START_HOUR] = start
        it[PrefsKeys.QUIET_END_HOUR] = end
    }
    suspend fun setLastChanged(epochMillis: Long) = context.dataStore.edit { it[PrefsKeys.LAST_CHANGED] = epochMillis }
    suspend fun setStreak(value: Int) = context.dataStore.edit { it[PrefsKeys.STREAK] = value }
}
