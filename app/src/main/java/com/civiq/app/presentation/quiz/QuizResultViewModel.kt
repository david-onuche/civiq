package com.civiq.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.usecase.quiz.QuizUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the just-completed [QuizAttempt] for [QuizResultScreen]. */
@HiltViewModel
class QuizResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizUseCases: QuizUseCases,
) : ViewModel() {

    private val attemptId: String = checkNotNull(savedStateHandle[Screen.ARG_ATTEMPT_ID])

    private val _uiState = MutableStateFlow<Resource<QuizAttempt>>(Resource.Loading())
    val uiState: StateFlow<Resource<QuizAttempt>> = _uiState.asStateFlow()

    init {
        loadAttempt()
    }

    fun loadAttempt() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading()
            _uiState.value = quizUseCases.getQuizAttempt(attemptId)
        }
    }
}
