package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by [com.civiq.app.presentation.premium.PremiumViewModel]. */
data class PremiumUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val observeSubscription: ObserveSubscriptionUseCase,
    val observeFeatureFlags: ObserveFeatureFlagsUseCase,
    val subscribe: SubscribeUseCase,
    val cancelSubscription: CancelSubscriptionUseCase,
)
