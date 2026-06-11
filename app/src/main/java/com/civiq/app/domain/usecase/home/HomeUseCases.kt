package com.civiq.app.domain.usecase.home

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by [com.civiq.app.presentation.home.HomeViewModel]. */
data class HomeUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val getTodayChallenge: GetTodayChallengeUseCase,
    val observeDailyChallengeProgress: ObserveDailyChallengeProgressUseCase,
    val observeRecentAchievements: ObserveRecentAchievementsUseCase,
    val observeRecentQuizAttempt: ObserveRecentQuizAttemptUseCase,
)
