package com.civiq.app.services.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.civiq.app.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-schedules [DailyReminderWorker] after the device reboots, since
 * WorkManager's scheduled jobs do not survive a reboot on their own.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        applicationScope.launch {
            notificationScheduler.scheduleDailyReminder()
        }
    }
}
