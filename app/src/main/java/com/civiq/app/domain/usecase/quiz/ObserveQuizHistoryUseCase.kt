package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the user's quiz attempt history, most recent first. */
class ObserveQuizHistoryUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    operator fun invoke(userId: String, limit: Int = 20): Flow<Resource<List<QuizAttempt>>> =
        quizRepository.observeQuizHistory(userId, limit)
}
