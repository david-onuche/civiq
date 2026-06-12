package com.civiq.app.presentation.leaderboard

import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.utils.Resource

/** UI state for the Leaderboard screen. */
data class LeaderboardUiState(
    val period: LeaderboardPeriod = LeaderboardPeriod.WEEKLY,
    val scope: LeaderboardScope = LeaderboardScope.GLOBAL,
    val entries: Resource<List<LeaderboardEntry>> = Resource.Loading(),
    val currentUserId: String? = null,
    val currentUserRank: LeaderboardEntry? = null,
)
