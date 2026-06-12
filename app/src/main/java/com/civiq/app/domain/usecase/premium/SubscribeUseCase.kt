package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.domain.repository.SubscriptionRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Starts (or renews) a user's premium subscription via [provider], e.g. after a successful checkout. */
class SubscribeUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) {
    suspend operator fun invoke(userId: String, tier: SubscriptionTier, provider: PaymentProvider): Resource<Subscription> =
        subscriptionRepository.startSubscription(userId, tier, provider)
}
