package com.civiq.app.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.usecase.quiz.QuizUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives the Quiz tab's category grid: surfaces whether the current user is
 * Premium (unlimited quizzes) or, for free-tier users, how many quiz
 * sessions they have left today.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class QuizHubViewModel @Inject constructor(
    private val quizUseCases: QuizUseCases,
) : ViewModel() {

    val uiState: StateFlow<QuizHubUiState> = quizUseCases.observeCurrentUser()
        .filterNotNull()
        .flatMapLatest { user ->
            flow {
                emit(QuizHubUiState(isLoading = true, isPremium = user.isPremium))
                if (user.isPremium) {
                    emit(QuizHubUiState(isLoading = false, isPremium = true))
                } else {
                    val remaining = (quizUseCases.getRemainingFreeAttempts(user.id) as? Resource.Success)?.data
                    emit(QuizHubUiState(isLoading = false, isPremium = false, remainingFreeAttempts = remaining))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuizHubUiState())
}
