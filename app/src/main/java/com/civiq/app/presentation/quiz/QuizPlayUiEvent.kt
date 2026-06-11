package com.civiq.app.presentation.quiz

/** User intents handled by [QuizPlayViewModel]. */
sealed interface QuizPlayUiEvent {
    data class SelectAnswer(val index: Int) : QuizPlayUiEvent
    data object NextQuestion : QuizPlayUiEvent
    data object ErrorShown : QuizPlayUiEvent
}
