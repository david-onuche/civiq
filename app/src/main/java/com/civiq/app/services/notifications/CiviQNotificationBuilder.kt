package com.civiq.app.services.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.civiq.app.MainActivity
import com.civiq.app.R
import com.civiq.app.domain.model.NotificationType
import com.civiq.app.utils.NotificationChannels
import com.civiq.app.utils.safeEnumValueOf
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Extra key for the in-app route to open when a notification is tapped. See [com.civiq.app.navigation.Screen]. */
const val EXTRA_DEEP_LINK_ROUTE = "deep_link_route"

/** Builds and displays local system notifications for incoming FCM pushes and WorkManager-scheduled reminders. */
@Singleton
class CiviQNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Displays a notification from an FCM [RemoteMessage], using its data payload for type/deep-link routing. */
    fun showFromRemoteMessage(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = safeEnumValueOf(message.data["type"], NotificationType.SYSTEM)
        show(type = type, title = title, body = body, deepLinkRoute = message.data["deepLinkRoute"])
    }

    @SuppressLint("MissingPermission")
    fun show(type: NotificationType, title: String, body: String, deepLinkRoute: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (deepLinkRoute != null) putExtra(EXTRA_DEEP_LINK_ROUTE, deepLinkRoute)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelIdFor(type))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priorityFor(type))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }

    private fun channelIdFor(type: NotificationType): String = when (type) {
        NotificationType.DAILY_QUIZ -> NotificationChannels.DAILY_QUIZ_CHANNEL_ID
        NotificationType.STREAK_REMINDER -> NotificationChannels.STREAK_CHANNEL_ID
        NotificationType.ACHIEVEMENT_UNLOCKED -> NotificationChannels.ACHIEVEMENT_CHANNEL_ID
        NotificationType.WEEKLY_CHALLENGE -> NotificationChannels.WEEKLY_CHALLENGE_CHANNEL_ID
        NotificationType.SYSTEM -> NotificationChannels.GENERAL_CHANNEL_ID
    }

    private fun priorityFor(type: NotificationType): Int = when (type) {
        NotificationType.STREAK_REMINDER -> NotificationCompat.PRIORITY_HIGH
        else -> NotificationCompat.PRIORITY_DEFAULT
    }
}
