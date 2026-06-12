package com.civiq.app.presentation.challenges

import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.utils.UiText

/** UI state for [ChallengesScreen][com.civiq.app.presentation.challenges.ChallengesScreen]. */
data class ChallengesUiState(
    val isLoading: Boolean = true,
    val challenge: DailyChallenge? = null,
    val progress: DailyChallengeProgress? = null,
    val streakCount: Int = 0,
    val error: UiText? = null,
)
