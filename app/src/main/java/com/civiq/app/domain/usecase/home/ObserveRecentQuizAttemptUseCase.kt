package com.civiq.app.domain.usecase.home

import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Observes the user's most recent quiz attempt, used to power the "Continue Learning" card. */
class ObserveRecentQuizAttemptUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    operator fun invoke(userId: String): Flow<Resource<QuizAttempt?>> =
        quizRepository.observeQuizHistory(userId, limit = 1).map { resource ->
            resource.map { attempts -> attempts.firstOrNull() }
        }
}
