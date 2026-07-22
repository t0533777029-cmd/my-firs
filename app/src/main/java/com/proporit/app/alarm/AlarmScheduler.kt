package com.proporit.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Wraps AlarmManager scheduling for the two kinds of reminders:
 *  - the "due" alarm, fired once when the interval (e.g. 3 days) elapses
 *  - the "hourly" alarm, fired repeatedly (re-scheduling itself) while overdue,
 *    until the user presses "Поменял пропорит".
 *
 * Exact alarms are used deliberately (not WorkManager) because reminders need
 * to fire at a precise time, including while the device is idle/dozing.
 */
object AlarmScheduler {

    const val ACTION_DUE = "com.proporit.app.action.DUE"
    const val ACTION_HOURLY = "com.proporit.app.action.HOURLY"

    private const val REQUEST_DUE = 1001
    private const val REQUEST_HOURLY = 1002

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }
        return true
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun scheduleDue(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = ACTION_DUE }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_DUE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(am, triggerAtMillis, pi)
    }

    fun scheduleHourly(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = ACTION_HOURLY }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_HOURLY, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(am, triggerAtMillis, pi)
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dueIntent = Intent(context, ReminderReceiver::class.java).apply { action = ACTION_DUE }
        val huntIntent = Intent(context, ReminderReceiver::class.java).apply { action = ACTION_HOURLY }
        am.cancel(PendingIntent.getBroadcast(context, REQUEST_DUE, dueIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        am.cancel(PendingIntent.getBroadcast(context, REQUEST_HOURLY, huntIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    private fun setExact(am: AlarmManager, triggerAtMillis: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Falls back to an inexact alarm if the user hasn't granted the
            // "Alarms & reminders" special permission yet.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            return
        }
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
    }
}
