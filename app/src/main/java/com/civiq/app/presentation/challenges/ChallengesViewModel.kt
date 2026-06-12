package com.civiq.app.presentation.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.usecase.challenge.ChallengeUseCases
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the Challenges tab: today's daily challenge, the user's completion status for it, and their current streak. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChallengesViewModel @Inject constructor(
    private val challengeUseCases: ChallengeUseCases,
) : ViewModel() {

    private val challengeState = MutableStateFlow<Resource<DailyChallenge>>(Resource.Loading())

    val uiState: StateFlow<ChallengesUiState> = combine(
        challengeUseCases.observeCurrentUser(),
        challengeState,
    ) { user, challengeResource -> user to challengeResource }
        .flatMapLatest { (user, challengeResource) ->
            if (user == null) {
                flowOf(
                    ChallengesUiState(
                        isLoading = challengeResource is Resource.Loading,
                        challenge = (challengeResource as? Resource.Success)?.data,
                        error = (challengeResource as? Resource.Error)?.message,
                    ),
                )
            } else {
                val today = System.currentTimeMillis().toDateId()
                challengeUseCases.observeDailyChallengeProgress(user.id, today).map { progressResource ->
                    ChallengesUiState(
                        isLoading = challengeResource is Resource.Loading,
                        challenge = (challengeResource as? Resource.Success)?.data,
                        progress = (progressResource as? Resource.Success)?.data,
                        streakCount = user.streakCount,
                        error = (challengeResource as? Resource.Error)?.message,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengesUiState())

    init {
        loadChallenge()
    }

    fun loadChallenge() {
        viewModelScope.launch {
            challengeState.value = Resource.Loading()
            challengeState.value = challengeUseCases.getTodayChallenge()
        }
    }
}
