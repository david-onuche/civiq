package com.civiq.app.presentation.home

import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserLevel
import com.civiq.app.domain.model.UserLevels

/** UI state for [HomeScreen][com.civiq.app.presentation.home.HomeScreen]. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val userLevel: UserLevel = UserLevels.fromTotalXp(0L),
    val dailyChallenge: DailyChallenge? = null,
    val isDailyChallengeLoading: Boolean = true,
    val dailyChallengeProgress: DailyChallengeProgress? = null,
    val recentAchievements: List<AchievementWithStatus> = emptyList(),
    val continueCategory: QuizCategory? = null,
    val continueDifficulty: QuestionDifficulty? = null,
)
