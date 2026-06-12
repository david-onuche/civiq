package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.domain.repository.LeaderboardRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes a ranked XP leaderboard for the given [LeaderboardPeriod]/[LeaderboardScope]. */
class ObserveLeaderboardUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
) {
    operator fun invoke(
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String? = null,
        limit: Int = 100,
    ): Flow<Resource<List<LeaderboardEntry>>> =
        leaderboardRepository.observeLeaderboard(period, scope, countryCode, limit)
}
