package com.civiq.app.presentation.quiz

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.UiText

/** UI state for [QuizPlayScreen][com.civiq.app.presentation.quiz.QuizPlayScreen]. */
data class QuizPlayUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: UiText? = null,
    val category: QuizCategory = QuizCategory.DEMOCRACY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.BEGINNER,
    val isDailyChallenge: Boolean = false,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val answers: List<QuestionAnswer> = emptyList(),
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
    val progress: Float get() = if (questions.isEmpty()) 0f else (currentIndex + 1) / questions.size.toFloat()
}
