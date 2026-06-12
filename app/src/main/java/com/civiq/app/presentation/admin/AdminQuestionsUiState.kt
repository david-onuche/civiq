package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.Resource

data class AdminQuestionsUiState(
    val questions: Resource<List<Question>> = Resource.Loading(),
    val categoryFilter: QuizCategory? = null,
    val deletingQuestionId: String? = null,
)
