package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by the Achievements and Leaderboard screens' ViewModels. */
data class GamificationUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val observeAchievementsWithStatus: ObserveAchievementsWithStatusUseCase,
    val observeLeaderboard: ObserveLeaderboardUseCase,
    val getUserRank: GetUserRankUseCase,
)
