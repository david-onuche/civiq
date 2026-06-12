package com.civiq.app.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.domain.usecase.gamification.GamificationUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Drives the Leaderboard screen: ranked XP entries for a selectable time period and population scope. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModel @Inject constructor(
    gamificationUseCases: GamificationUseCases,
) : ViewModel() {

    private val filters = MutableStateFlow(LeaderboardFilters())

    val uiState: StateFlow<LeaderboardUiState> = combine(
        filters,
        gamificationUseCases.observeCurrentUser().filterNotNull(),
    ) { filters, user -> filters to user }
        .flatMapLatest { (filters, user) ->
            val rankResult = gamificationUseCases.getUserRank(user.id, filters.period, filters.scope, user.countryCode)
            val currentUserRank = (rankResult as? Resource.Success)?.data
            gamificationUseCases.observeLeaderboard(filters.period, filters.scope, user.countryCode)
                .map { entries ->
                    LeaderboardUiState(
                        period = filters.period,
                        scope = filters.scope,
                        entries = entries,
                        currentUserId = user.id,
                        currentUserRank = currentUserRank,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaderboardUiState())

    fun selectPeriod(period: LeaderboardPeriod) {
        filters.update { it.copy(period = period) }
    }

    fun selectScope(scope: LeaderboardScope) {
        filters.update { it.copy(scope = scope) }
    }
}

private data class LeaderboardFilters(
    val period: LeaderboardPeriod = LeaderboardPeriod.WEEKLY,
    val scope: LeaderboardScope = LeaderboardScope.GLOBAL,
)
