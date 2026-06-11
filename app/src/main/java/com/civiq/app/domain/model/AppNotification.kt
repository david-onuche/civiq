package com.civiq.app.domain.model

/**
 * An in-app notification record, stored at `notifications/{notificationId}`
 * (queried by `userId`) and mirrored to a push notification via FCM.
 */
data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val title: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    val deepLinkRoute: String? = null,
    val createdAt: Long = 0L,
)

/**
 * The four push notification categories required by the MVP, plus a
 * general [SYSTEM] catch-all for admin broadcasts.
 */
enum class NotificationType {
    DAILY_QUIZ,
    STREAK_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    WEEKLY_CHALLENGE,
    SYSTEM,
}
