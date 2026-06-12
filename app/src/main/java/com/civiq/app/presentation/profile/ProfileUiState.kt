package com.civiq.app.presentation.profile

import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserLevel
import com.civiq.app.domain.model.UserLevels

/** UI state for [ProfileScreen][com.civiq.app.presentation.profile.ProfileScreen]. */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val userLevel: UserLevel = UserLevels.fromTotalXp(0L),
)
