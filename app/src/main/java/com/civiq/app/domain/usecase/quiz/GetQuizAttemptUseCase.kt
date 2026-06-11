package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Loads a single completed [QuizAttempt] by ID, e.g. for the quiz result screen. */
class GetQuizAttemptUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(attemptId: String): Resource<QuizAttempt> = quizRepository.getQuizAttempt(attemptId)
}
