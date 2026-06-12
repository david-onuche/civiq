package com.civiq.app.domain.usecase.notification

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by [com.civiq.app.presentation.notifications.NotificationsViewModel] and the FCM service. */
data class NotificationUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val observeNotifications: ObserveNotificationsUseCase,
    val markNotificationAsRead: MarkNotificationAsReadUseCase,
    val markAllNotificationsAsRead: MarkAllNotificationsAsReadUseCase,
    val syncFcmToken: SyncFcmTokenUseCase,
)
