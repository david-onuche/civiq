package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `subscriptions/{subscriptionId}`. See docs/DATABASE.md. */
data class SubscriptionDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tier: String = "FREE",
    val provider: String? = null,
    val startedAt: Long = 0L,
    val expiresAt: Long? = null,
    val autoRenew: Boolean = false,
    val status: String = "ACTIVE",
)
