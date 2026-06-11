package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Returns how many free quiz sessions a non-premium user has left today. */
class GetRemainingFreeAttemptsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(userId: String): Resource<Int> = quizRepository.getRemainingFreeAttemptsToday(userId)
}
