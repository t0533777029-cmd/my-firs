package com.proporit.app.data

import android.content.Context
import com.proporit.app.alarm.AlarmScheduler
import com.proporit.app.notify.Notifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefsStore(appContext)
    private val dao = AppDatabase.getInstance(appContext).changeDao()

    val settingsFlow: Flow<ReminderSettings> = prefs.settingsFlow
    val historyFlow: Flow<List<ChangeEvent>> = dao.observeAll()

    fun nextDueMillis(s: ReminderSettings): Long =
        s.lastChangedEpochMillis + TimeUnit.DAYS.toMillis(s.intervalDays.toLong())

    /** Called when the user taps "Поменял пропорит", from the app or from the notification action. */
    suspend fun markChanged() {
        val s = settingsFlow.first()
        val now = System.currentTimeMillis()
        val hadPrevious = s.lastChangedEpochMillis > 0L
        val onTime = !hadPrevious || now <= nextDueMillis(s)
        val newStreak = if (onTime) s.streak + 1 else 0

        dao.insert(ChangeEvent(timestampEpochMillis = now, onTime = onTime))
        prefs.setLastChanged(now)
        prefs.setStreak(newStreak)

        AlarmScheduler.cancelAll(appContext)
        Notifier.cancel(appContext)
        AlarmScheduler.scheduleDue(appContext, now + TimeUnit.DAYS.toMillis(s.intervalDays.toLong()))
    }

    suspend fun updateIntervalDays(days: Int) {
        prefs.setIntervalDays(days)
        rescheduleFromCurrentState()
    }

    suspend fun updateFrequencyHours(hours: Int) {
        prefs.setFrequencyHours(hours)
        rescheduleFromCurrentState()
    }

    suspend fun updateSoundEnabled(v: Boolean) = prefs.setSoundEnabled(v)
    suspend fun updateVibrationEnabled(v: Boolean) = prefs.setVibrationEnabled(v)
    suspend fun updateQuietHours(enabled: Boolean, start: Int, end: Int) {
        prefs.setQuietHoursEnabled(enabled)
        prefs.setQuietRange(start, end)
    }

    /**
     * Recomputes and (re)schedules the correct next alarm from whatever state
     * is currently stored. Safe to call after boot, after a crash/reinstall,
     * or any time settings change — this is what makes the schedule
     * "self-healing" instead of depending on a chain of alarms surviving.
     */
    suspend fun rescheduleFromCurrentState() {
        val s = settingsFlow.first()
        val now = System.currentTimeMillis()
        AlarmScheduler.cancelAll(appContext)

        if (s.lastChangedEpochMillis == 0L) {
            // Very first run: baseline starts now, nothing is overdue yet.
            prefs.setLastChanged(now)
            AlarmScheduler.scheduleDue(appContext, now + TimeUnit.DAYS.toMillis(s.intervalDays.toLong()))
            return
        }

        val due = nextDueMillis(s)
        if (now < due) {
            AlarmScheduler.scheduleDue(appContext, due)
        } else {
            AlarmScheduler.scheduleHourly(appContext, nextAllowedHourlyTime(s, now))
        }
    }

    fun nextAllowedHourlyTime(s: ReminderSettings, fromMillis: Long): Long {
        var candidate = fromMillis + TimeUnit.HOURS.toMillis(s.frequencyHours.toLong())
        if (s.quietHoursEnabled) {
            candidate = pushOutsideQuietHours(candidate, s.quietStartHour, s.quietEndHour)
        }
        return candidate
    }

    /** If [millis] falls inside the quiet-hours window, moves it to the window's end hour. */
    private fun pushOutsideQuietHours(millis: Long, startHour: Int, endHour: Int): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val inQuietWindow = if (startHour <= endHour) {
            hour in startHour until endHour
        } else {
            // overnight window, e.g. 23 -> 7
            hour >= startHour || hour < endHour
        }

        if (!inQuietWindow) return millis

        val result = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If the quiet window wraps past midnight and "now" is before midnight,
        // the end hour lands the next calendar day.
        if (startHour > endHour && hour >= startHour) {
            result.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result.timeInMillis
    }
}
