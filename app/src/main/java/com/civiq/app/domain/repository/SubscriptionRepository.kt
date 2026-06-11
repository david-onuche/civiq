package com.civiq.app.domain.repository

import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** Premium subscription lifecycle (`subscriptions/{subscriptionId}`). */
interface SubscriptionRepository {

    fun observeSubscription(userId: String): Flow<Resource<Subscription?>>

    suspend fun startSubscription(
        userId: String,
        tier: SubscriptionTier,
        provider: PaymentProvider,
    ): Resource<Subscription>

    suspend fun cancelSubscription(userId: String): Resource<Unit>
}
