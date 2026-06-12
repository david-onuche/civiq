package com.civiq.app.domain.usecase.notification

import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.domain.repository.UserRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/**
 * Registers a device's FCM token for push delivery: appends it to the user's
 * `fcmTokens` array (read by Cloud Functions to target pushes) and records
 * per-device metadata under `users/{uid}/devices/{token}`.
 */
class SyncFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(userId: String, token: String): Resource<Unit> {
        notificationRepository.registerDeviceToken(userId, token)
        return userRepository.addFcmToken(userId, token)
    }
}
