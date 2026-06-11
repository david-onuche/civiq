package com.civiq.app.domain.repository

import com.civiq.app.domain.model.AppNotification
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** In-app notification feed and push-notification device registration. */
interface NotificationRepository {

    fun observeNotifications(userId: String, limit: Int = 50): Flow<Resource<List<AppNotification>>>

    suspend fun markAsRead(notificationId: String): Resource<Unit>

    suspend fun markAllAsRead(userId: String): Resource<Unit>

    /** Registers an FCM device token under `users/{uid}/devices/{token}`. */
    suspend fun registerDeviceToken(userId: String, token: String): Resource<Unit>

    suspend fun unregisterDeviceToken(userId: String, token: String): Resource<Unit>
}
