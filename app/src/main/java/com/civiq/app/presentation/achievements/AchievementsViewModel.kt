package com.civiq.app.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.domain.usecase.gamification.GamificationUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Drives the Achievements screen: every badge definition with the user's unlock status and progress. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModel @Inject constructor(
    gamificationUseCases: GamificationUseCases,
) : ViewModel() {

    val uiState: StateFlow<Resource<List<AchievementWithStatus>>> = gamificationUseCases.observeCurrentUser()
        .filterNotNull()
        .flatMapLatest { user -> gamificationUseCases.observeAchievementsWithStatus(user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())
}
