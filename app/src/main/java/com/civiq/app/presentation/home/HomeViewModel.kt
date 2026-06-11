package com.civiq.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.usecase.home.HomeUseCases
import com.civiq.app.domain.model.UserLevels
import com.civiq.app.utils.Resource
import com.civiq.app.utils.toDateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Home tab: the user's XP/level/coin/streak summary, today's
 * daily challenge, a "continue learning" suggestion based on quiz history,
 * and recently unlocked achievements.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val homeUseCases: HomeUseCases,
) : ViewModel() {

    private val dailyChallengeState = MutableStateFlow<Resource<DailyChallenge>>(Resource.Loading())

    val uiState: StateFlow<HomeUiState> = combine(
        homeUseCases.observeCurrentUser(),
        dailyChallengeState,
    ) { user, challengeResource -> user to challengeResource }
        .flatMapLatest { (user, challengeResource) ->
            if (user == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                val today = System.currentTimeMillis().toDateId()
                combine(
                    homeUseCases.observeRecentAchievements(user.id),
                    homeUseCases.observeRecentQuizAttempt(user.id),
                    homeUseCases.observeDailyChallengeProgress(user.id, today),
                ) { achievementsResource, recentAttemptResource, progressResource ->
                    val recentAttempt = (recentAttemptResource as? Resource.Success)?.data
                    HomeUiState(
                        isLoading = false,
                        user = user,
                        userLevel = UserLevels.fromTotalXp(user.xp),
                        dailyChallenge = (challengeResource as? Resource.Success)?.data,
                        isDailyChallengeLoading = challengeResource is Resource.Loading,
                        dailyChallengeProgress = (progressResource as? Resource.Success)?.data,
                        recentAchievements = (achievementsResource as? Resource.Success)?.data.orEmpty(),
                        continueCategory = recentAttempt?.category,
                        continueDifficulty = recentAttempt?.difficulty,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    init {
        loadDailyChallenge()
    }

    fun loadDailyChallenge() {
        viewModelScope.launch {
            dailyChallengeState.value = Resource.Loading()
            dailyChallengeState.value = homeUseCases.getTodayChallenge()
        }
    }
}
