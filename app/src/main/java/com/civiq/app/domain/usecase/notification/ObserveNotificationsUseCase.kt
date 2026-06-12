package com.civiq.app.domain.usecase.notification

import com.civiq.app.domain.model.AppNotification
import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the signed-in user's in-app notification feed, most recent first. */
class ObserveNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(userId: String, limit: Int = 50): Flow<Resource<List<AppNotification>>> =
        notificationRepository.observeNotifications(userId, limit)
}
