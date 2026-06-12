package com.civiq.app.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the question bank create/edit form. When [Screen.ARG_QUESTION_ID] is
 * present, the existing [Question] is loaded and [AdminQuestionEditorUiState.originalQuestion]
 * is preserved so fields not shown in the form (id, createdAt, source, tags,
 * etc.) survive [save] - `AdminRepositoryImpl.updateQuestion` overwrites the
 * whole document.
 */
@HiltViewModel
class AdminQuestionEditorViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val questionId: String? = savedStateHandle[Screen.ARG_QUESTION_ID]

    private val _uiState = MutableStateFlow(AdminQuestionEditorUiState(questionId = questionId))
    val uiState: StateFlow<AdminQuestionEditorUiState> = _uiState.asStateFlow()

    init {
        val id = questionId
        if (id != null) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                when (val result = adminUseCases.getQuestion(id)) {
                    is Resource.Success -> {
                        val question = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                originalQuestion = question,
                                type = question.type,
                                category = question.category,
                                difficulty = question.difficulty,
                                tone = question.tone,
                                questionText = question.questionText,
                                options = question.options,
                                correctAnswerIndex = question.correctAnswerIndex,
                                explanation = question.explanation,
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun onTypeChanged(type: QuestionType) {
        _uiState.update { state ->
            val options = when {
                type == QuestionType.TRUE_FALSE -> listOf("True", "False")
                state.type == QuestionType.TRUE_FALSE -> listOf("", "", "", "")
                else -> state.options
            }
            state.copy(
                type = type,
                options = options,
                correctAnswerIndex = state.correctAnswerIndex.coerceIn(0, options.lastIndex),
            )
        }
    }

    fun onCategoryChanged(category: QuizCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDifficultyChanged(difficulty: QuestionDifficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun onToneChanged(tone: QuestionTone) {
        _uiState.update { it.copy(tone = tone) }
    }

    fun onQuestionTextChanged(text: String) {
        _uiState.update { it.copy(questionText = text) }
    }

    fun onExplanationChanged(text: String) {
        _uiState.update { it.copy(explanation = text) }
    }

    fun onOptionTextChanged(index: Int, text: String) {
        _uiState.update { state ->
            state.copy(options = state.options.toMutableList().also { it[index] = text })
        }
    }

    fun onCorrectAnswerSelected(index: Int) {
        _uiState.update { it.copy(correctAnswerIndex = index) }
    }

    fun onAddOption() {
        _uiState.update { state ->
            if (state.options.size >= AdminQuestionEditorUiState.MAX_OPTIONS) {
                state
            } else {
                state.copy(options = state.options + "")
            }
        }
    }

    fun onRemoveOption(index: Int) {
        _uiState.update { state ->
            if (state.options.size <= AdminQuestionEditorUiState.MIN_OPTIONS) {
                state
            } else {
                val newOptions = state.options.toMutableList().also { it.removeAt(index) }
                state.copy(
                    options = newOptions,
                    correctAnswerIndex = state.correctAnswerIndex.coerceIn(0, newOptions.lastIndex),
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return

        val base = state.originalQuestion ?: Question()
        val question = base.copy(
            type = state.type,
            category = state.category,
            difficulty = state.difficulty,
            questionText = state.questionText.trim(),
            options = state.options.map { it.trim() },
            correctAnswerIndex = state.correctAnswerIndex,
            explanation = state.explanation.trim(),
            tone = state.tone,
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (state.isEditMode) {
                adminUseCases.updateQuestion(question)
            } else {
                adminUseCases.createQuestion(question)
            }
            when (result) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
