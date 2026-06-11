package com.civiq.app.presentation.quiz

/** One-shot navigation events emitted by [QuizPlayViewModel]. */
sealed interface QuizPlayNavigationEvent {
    data class NavigateToResult(val attemptId: String) : QuizPlayNavigationEvent
}
