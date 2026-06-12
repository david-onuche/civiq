package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.UiText

data class AdminChallengesUiState(
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val category: QuizCategory = QuizCategory.CIVIC_RESPONSIBILITY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.INTERMEDIATE,
    val xpReward: String = "",
    val coinReward: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: UiText? = null,
) {
    val isValid: Boolean
        get() = date.isNotBlank() &&
            title.isNotBlank() &&
            description.isNotBlank() &&
            xpReward.toLongOrNull() != null &&
            coinReward.toLongOrNull() != null
}
