package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `notifications/{notificationId}`. See docs/DATABASE.md. */
data class AppNotificationDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "SYSTEM",
    val title: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    val deepLinkRoute: String? = null,
    val createdAt: Long = 0L,
)
