package com.civiq.app.services.fcm

import com.civiq.app.di.ApplicationScope
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.domain.usecase.notification.NotificationUseCases
import com.civiq.app.services.notifications.CiviQNotificationBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives Firebase Cloud Messaging pushes (daily quiz reminders, streak
 * nudges, achievement unlocks, weekly challenges, and admin broadcasts) and
 * keeps this device's push token registered against the signed-in user.
 */
@AndroidEntryPoint
class CiviQFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var notificationUseCases: NotificationUseCases

    @Inject
    lateinit var notificationBuilder: CiviQNotificationBuilder

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = authRepository.getCurrentUserId() ?: return
        applicationScope.launch {
            notificationUseCases.syncFcmToken(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: data=${message.data}, notification=${message.notification}")
        notificationBuilder.showFromRemoteMessage(message)
    }
}
