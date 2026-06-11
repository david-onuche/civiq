package com.civiq.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.civiq.app.utils.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application entry point. Bootstraps Hilt's dependency graph, logging, and
 * the notification channels used by Firebase Cloud Messaging (daily quiz
 * reminders, streak nudges, achievement unlocks, weekly challenges).
 */
@HiltAndroidApp
class CiviQApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                NotificationChannels.GENERAL_CHANNEL_ID,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
            NotificationChannel(
                NotificationChannels.DAILY_QUIZ_CHANNEL_ID,
                "Daily Quiz",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Reminders to complete today's quiz" },
            NotificationChannel(
                NotificationChannels.STREAK_CHANNEL_ID,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Don't lose your learning streak!" },
            NotificationChannel(
                NotificationChannels.ACHIEVEMENT_CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "New badges and achievements unlocked" },
            NotificationChannel(
                NotificationChannels.WEEKLY_CHALLENGE_CHANNEL_ID,
                "Weekly Challenge",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "New weekly civic challenges" },
        )
        manager.createNotificationChannels(channels)
    }
}
