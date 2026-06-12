package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Lists question bank entries, filterable by [QuizCategory], with delete support. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminQuestionsViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
) : ViewModel() {

    private val categoryFilter = MutableStateFlow<QuizCategory?>(null)
    private val deletingQuestionId = MutableStateFlow<String?>(null)

    private val questionsResource: StateFlow<Resource<List<Question>>> = categoryFilter
        .flatMapLatest { category -> adminUseCases.observeAllQuestions(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    val uiState: StateFlow<AdminQuestionsUiState> = combine(
        questionsResource,
        categoryFilter,
        deletingQuestionId,
    ) { questions, category, deletingId ->
        AdminQuestionsUiState(questions = questions, categoryFilter = category, deletingQuestionId = deletingId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminQuestionsUiState())

    fun onCategoryFilterChanged(category: QuizCategory?) {
        categoryFilter.update { category }
    }

    fun onDeleteQuestion(questionId: String) {
        if (deletingQuestionId.value != null) return
        deletingQuestionId.update { questionId }
        viewModelScope.launch {
            adminUseCases.deleteQuestion(questionId)
            deletingQuestionId.update { null }
        }
    }
}
