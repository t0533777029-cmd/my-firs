package com.proporit.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.proporit.app.ProporitApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-derives and re-schedules the correct alarm after a reboot, an app
 * update, or a crash — this is the "crash-safe" behaviour: nothing about
 * the schedule is trusted to survive in memory, it's recomputed from the
 * stored last-changed timestamp every time.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val app = context.applicationContext as ProporitApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.rescheduleFromCurrentState()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
