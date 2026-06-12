package com.civiq.app.domain.usecase.notification

import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Marks a single notification as read, e.g. when the user taps it. */
class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String): Resource<Unit> =
        notificationRepository.markAsRead(notificationId)
}
