package com.civiq.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.civiq.app.data.local.datastore.UserPreferencesDataSource
import com.civiq.app.di.ApplicationScope
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.domain.usecase.notification.NotificationUseCases
import com.civiq.app.services.notifications.NotificationScheduler
import com.civiq.app.utils.NotificationChannels
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point. Bootstraps Hilt's dependency graph, logging, the
 * notification channels used by Firebase Cloud Messaging (daily quiz
 * reminders, streak nudges, achievement unlocks, weekly challenges), and
 * background work (daily reminder scheduling, FCM token sync).
 */
@HiltAndroidApp
class CiviQApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var notificationUseCases: NotificationUseCases

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var userPreferencesDataSource: UserPreferencesDataSource

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannels()

        applicationScope.launch { notificationScheduler.scheduleDailyReminder() }
        applicationScope.launch { syncFcmTokenOnSignIn() }
    }

    /** Keeps this device's FCM token registered against the signed-in user, e.g. after a fresh sign-in. */
    private suspend fun syncFcmTokenOnSignIn() {
        authRepository.currentUser.collectLatest { user ->
            if (user != null && !user.isGuest) {
                val token = firebaseMessaging.token.await()
                if (token != userPreferencesDataSource.lastSyncedFcmToken.first()) {
                    notificationUseCases.syncFcmToken(user.id, token)
                    userPreferencesDataSource.setLastSyncedFcmToken(token)
                }
            }
        }
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
