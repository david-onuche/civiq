package com.civiq.app.domain.usecase.notification

import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Marks every notification in the user's feed as read. */
class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(userId: String): Resource<Unit> =
        notificationRepository.markAllAsRead(userId)
}
