package com.civiq.app.domain.usecase.admin

import com.civiq.app.R
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.GamificationConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import javax.inject.Inject

/**
 * Creates or replaces the daily challenge for [DailyChallenge.date], automatically
 * selecting [GamificationConfig.DAILY_CHALLENGE_QUESTION_COUNT] curated questions
 * matching the chosen category and difficulty.
 */
class CreateOrUpdateDailyChallengeUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(challenge: DailyChallenge): Resource<DailyChallenge> {
        val questions = quizRepository.getQuestions(
            category = challenge.category,
            difficulty = challenge.difficulty,
            count = GamificationConfig.DAILY_CHALLENGE_QUESTION_COUNT,
        )
        val questionIds = (questions as? Resource.Success)?.data?.map { it.id } ?: emptyList()
        if (questionIds.isEmpty()) {
            return Resource.Error(UiText.StringResource(R.string.admin_challenges_error_no_questions))
        }
        return adminRepository.createOrUpdateDailyChallenge(challenge.copy(questionIds = questionIds))
    }
}
