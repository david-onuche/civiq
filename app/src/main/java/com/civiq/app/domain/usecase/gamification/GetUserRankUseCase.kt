package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.domain.repository.LeaderboardRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Fetches the current user's rank entry for a leaderboard, or null if they're unranked. */
class GetUserRankUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
) {
    suspend operator fun invoke(
        userId: String,
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String? = null,
    ): Resource<LeaderboardEntry?> = leaderboardRepository.getUserRank(userId, period, scope, countryCode)
}
