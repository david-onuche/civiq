package com.civiq.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.UserLevels
import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Drives the Profile tab: the signed-in user's identity, level/XP progress, and gamification stats. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                ProfileUiState(isLoading = false)
            } else {
                ProfileUiState(isLoading = false, user = user, userLevel = UserLevels.fromTotalXp(user.xp))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
