package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.AppNotificationDto
import com.civiq.app.domain.model.AppNotification
import com.civiq.app.domain.model.NotificationType
import com.civiq.app.utils.safeEnumValueOf

fun AppNotificationDto.toDomain(): AppNotification = AppNotification(
    id = id,
    userId = userId,
    type = safeEnumValueOf(type, NotificationType.SYSTEM),
    title = title,
    body = body,
    isRead = isRead,
    deepLinkRoute = deepLinkRoute,
    createdAt = createdAt,
)

fun AppNotification.toDto(): AppNotificationDto = AppNotificationDto(
    id = id,
    userId = userId,
    type = type.name,
    title = title,
    body = body,
    isRead = isRead,
    deepLinkRoute = deepLinkRoute,
    createdAt = createdAt,
)
