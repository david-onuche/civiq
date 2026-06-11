package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.SubscriptionDto
import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionStatus
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.utils.safeEnumValueOf

fun SubscriptionDto.toDomain(): Subscription = Subscription(
    id = id,
    userId = userId,
    tier = safeEnumValueOf(tier, SubscriptionTier.FREE),
    provider = provider?.let { safeEnumValueOf(it, PaymentProvider.GOOGLE_PLAY) },
    startedAt = startedAt,
    expiresAt = expiresAt,
    autoRenew = autoRenew,
    status = safeEnumValueOf(status, SubscriptionStatus.ACTIVE),
)

fun Subscription.toDto(): SubscriptionDto = SubscriptionDto(
    id = id,
    userId = userId,
    tier = tier.name,
    provider = provider?.name,
    startedAt = startedAt,
    expiresAt = expiresAt,
    autoRenew = autoRenew,
    status = status.name,
)
