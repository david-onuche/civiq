package com.civiq.app.domain.model

/**
 * A user's premium subscription record, stored at
 * `subscriptions/{subscriptionId}` (queried by `userId`). The user's
 * cached `isPremium`/`premiumExpiresAt` fields (see [User]) are kept in
 * sync with this document by a Cloud Function on subscription changes.
 */
data class Subscription(
    val id: String = "",
    val userId: String = "",
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val provider: PaymentProvider? = null,
    val startedAt: Long = 0L,
    val expiresAt: Long? = null,
    val autoRenew: Boolean = false,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
) {
    val isActive: Boolean
        get() = status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.GRACE_PERIOD
}

enum class SubscriptionTier(val displayName: String) {
    FREE("Free"),
    PREMIUM_MONTHLY("Premium (Monthly)"),
    PREMIUM_YEARLY("Premium (Yearly)"),
}

enum class PaymentProvider {
    GOOGLE_PLAY,
    STRIPE,
    PAYSTACK,
}

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELED,
    GRACE_PERIOD,
}
