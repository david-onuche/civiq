package com.civiq.app.domain.usecase.aicoach

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.premium.IsFeatureEnabledUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by [com.civiq.app.presentation.aicoach.AiCoachViewModel]. */
data class AiCoachUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val isFeatureEnabled: IsFeatureEnabledUseCase,
    val sendCoachMessage: SendCoachMessageUseCase,
)
