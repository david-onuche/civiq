package com.civiq.app.domain.usecase.challenge

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.home.GetTodayChallengeUseCase
import com.civiq.app.domain.usecase.home.ObserveDailyChallengeProgressUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by [com.civiq.app.presentation.challenges.ChallengesViewModel]. */
data class ChallengeUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val getTodayChallenge: GetTodayChallengeUseCase,
    val observeDailyChallengeProgress: ObserveDailyChallengeProgressUseCase,
)
