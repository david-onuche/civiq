package com.civiq.app.domain.repository

import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** Ranked XP leaderboards across time periods and population scopes. */
interface LeaderboardRepository {

    fun observeLeaderboard(
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String? = null,
        limit: Int = 100,
    ): Flow<Resource<List<LeaderboardEntry>>>

    suspend fun getUserRank(
        userId: String,
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String? = null,
    ): Resource<LeaderboardEntry?>
}
