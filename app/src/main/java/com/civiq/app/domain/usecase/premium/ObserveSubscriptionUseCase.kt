package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.repository.SubscriptionRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the signed-in user's premium subscription record, or `null` if they've never subscribed. */
class ObserveSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) {
    operator fun invoke(userId: String): Flow<Resource<Subscription?>> =
        subscriptionRepository.observeSubscription(userId)
}
