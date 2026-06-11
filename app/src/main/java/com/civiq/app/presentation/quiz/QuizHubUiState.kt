package com.civiq.app.presentation.quiz

/** UI state for [QuizHubScreen][com.civiq.app.presentation.quiz.QuizHubScreen]. */
data class QuizHubUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val remainingFreeAttempts: Int? = null,
)
