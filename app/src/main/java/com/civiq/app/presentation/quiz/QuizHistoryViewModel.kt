package com.civiq.app.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.usecase.quiz.QuizUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Streams the current user's past quiz attempts, most recent first, for [QuizHistoryScreen]. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class QuizHistoryViewModel @Inject constructor(
    private val quizUseCases: QuizUseCases,
) : ViewModel() {

    val uiState: StateFlow<Resource<List<QuizAttempt>>> = quizUseCases.observeCurrentUser()
        .filterNotNull()
        .flatMapLatest { user -> quizUseCases.observeQuizHistory(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())
}
