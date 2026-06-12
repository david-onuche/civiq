package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.repository.SubscriptionRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Cancels a user's premium subscription, taking effect at the end of the current billing period. */
class CancelSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) {
    suspend operator fun invoke(userId: String): Resource<Unit> = subscriptionRepository.cancelSubscription(userId)
}
