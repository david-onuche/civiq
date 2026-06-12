package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.UiText

data class AdminQuestionEditorUiState(
    val questionId: String? = null,
    val originalQuestion: Question? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val category: QuizCategory = QuizCategory.DEMOCRACY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.BEGINNER,
    val tone: QuestionTone = QuestionTone.EDUCATIONAL,
    val questionText: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "",
    val saveSuccess: Boolean = false,
    val errorMessage: UiText? = null,
) {
    val isEditMode: Boolean get() = questionId != null

    val isValid: Boolean
        get() = questionText.isNotBlank() &&
            explanation.isNotBlank() &&
            correctAnswerIndex in options.indices &&
            options.count { it.isNotBlank() } >= 2

    companion object {
        const val MIN_OPTIONS = 2
        const val MAX_OPTIONS = 6
    }
}
