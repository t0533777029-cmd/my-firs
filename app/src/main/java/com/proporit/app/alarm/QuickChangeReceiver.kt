package com.proporit.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.proporit.app.ProporitApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Поменял" quick-action button tapped directly on the notification. */
class QuickChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as ProporitApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.markChanged()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
