package com.proporit.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.proporit.app.ProporitApplication
import com.proporit.app.R
import com.proporit.app.notify.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as ProporitApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = app.repository
                val settings = repo.settingsFlow.first()
                val now = System.currentTimeMillis()

                when (intent.action) {
                    AlarmScheduler.ACTION_DUE -> {
                        Notifier.show(
                            context = context,
                            title = context.getString(R.string.notif_due_title),
                            text = context.getString(R.string.notif_due_text, settings.intervalDays),
                            soundEnabled = settings.soundEnabled,
                            vibrationEnabled = settings.vibrationEnabled,
                        )
                        AlarmScheduler.scheduleHourly(context, repo.nextAllowedHourlyTime(settings, now))
                    }
                    AlarmScheduler.ACTION_HOURLY -> {
                        Notifier.show(
                            context = context,
                            title = context.getString(R.string.notif_overdue_title),
                            text = context.getString(R.string.notif_overdue_text, settings.frequencyHours),
                            soundEnabled = settings.soundEnabled,
                            vibrationEnabled = settings.vibrationEnabled,
                        )
                        AlarmScheduler.scheduleHourly(context, repo.nextAllowedHourlyTime(settings, now))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
