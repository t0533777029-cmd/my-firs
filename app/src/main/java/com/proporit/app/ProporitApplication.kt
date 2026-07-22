package com.proporit.app

import android.app.Application
import com.proporit.app.data.ReminderRepository
import com.proporit.app.notify.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ProporitApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    lateinit var repository: ReminderRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ReminderRepository(this)
        Notifier.ensureChannel(this)
    }
}
