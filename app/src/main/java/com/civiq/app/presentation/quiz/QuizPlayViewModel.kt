package com.civiq.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.R
import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.usecase.quiz.QuizUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.safeEnumValueOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives a single quiz session: loads the question set (curated for a normal
 * quiz, or the day's set for a daily challenge), tracks the user's answers
 * one question at a time, and submits the finished attempt for scoring.
 */
@HiltViewModel
class QuizPlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizUseCases: QuizUseCases,
) : ViewModel() {

    private val categoryArg = safeEnumValueOf(savedStateHandle.get<String>(Screen.ARG_CATEGORY), QuizCategory.DEMOCRACY)
    private val difficultyArg = safeEnumValueOf(savedStateHandle.get<String>(Screen.ARG_DIFFICULTY), QuestionDifficulty.BEGINNER)
    private val challengeId: String? = savedStateHandle.get<String>(Screen.ARG_CHALLENGE_ID)

    private val _uiState = MutableStateFlow(
        QuizPlayUiState(
            category = categoryArg,
            difficulty = difficultyArg,
            isDailyChallenge = challengeId != null,
        ),
    )
    val uiState: StateFlow<QuizPlayUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<QuizPlayNavigationEvent>()
    val navigationEvent: SharedFlow<QuizPlayNavigationEvent> = _navigationEvent.asSharedFlow()

    private var userId: String? = null
    private var startedAt: Long = 0L
    private var questionStartedAt: Long = 0L

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            userId = quizUseCases.observeCurrentUser().firstOrNull()?.id
            startedAt = System.currentTimeMillis()
            questionStartedAt = startedAt

            val result = if (challengeId != null) {
                when (val challengeResult = quizUseCases.getTodayChallenge()) {
                    is Resource.Success -> quizUseCases.getQuestionsByIds(challengeResult.data.questionIds)
                    is Resource.Error -> Resource.Error(challengeResult.message)
                    is Resource.Loading -> Resource.Loading()
                }
            } else {
                quizUseCases.getQuizQuestions(categoryArg, difficultyArg)
            }

            when (result) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = UiText.StringResource(R.string.quiz_no_questions))
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, questions = result.data) }
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onEvent(event: QuizPlayUiEvent) {
        when (event) {
            is QuizPlayUiEvent.SelectAnswer -> selectAnswer(event.index)
            QuizPlayUiEvent.NextQuestion -> nextQuestion()
            QuizPlayUiEvent.ErrorShown -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun selectAnswer(index: Int) {
        val state = _uiState.value
        if (state.isAnswerRevealed) return
        val question = state.currentQuestion ?: return

        val answer = QuestionAnswer(
            questionId = question.id,
            selectedIndex = index,
            isCorrect = question.isCorrectAnswer(index),
            timeTakenMs = System.currentTimeMillis() - questionStartedAt,
        )
        _uiState.update {
            it.copy(
                selectedAnswerIndex = index,
                isAnswerRevealed = true,
                answers = it.answers + answer,
            )
        }
    }

    private fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswerRevealed) return

        if (state.isLastQuestion) {
            finishQuiz()
        } else {
            questionStartedAt = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswerIndex = null,
                    isAnswerRevealed = false,
                )
            }
        }
    }

    private fun finishQuiz() {
        val state = _uiState.value
        val uid = userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = quizUseCases.completeQuiz(
                userId = uid,
                category = state.category,
                difficulty = state.difficulty,
                answers = state.answers,
                startedAt = startedAt,
                isDailyChallenge = state.isDailyChallenge,
                challengeId = challengeId,
            )
            when (result) {
                is Resource.Success -> _navigationEvent.emit(QuizPlayNavigationEvent.NavigateToResult(result.data.id))
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }
}
