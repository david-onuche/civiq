package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.domain.usecase.gamification.EvaluateAchievementsUseCase
import com.civiq.app.utils.GamificationConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.toDateId
import javax.inject.Inject

/**
 * Scores a finished quiz session, persists the [QuizAttempt], and applies its
 * rewards: XP/coins, streak update, achievement unlocks, and (for daily
 * challenges) marking the day's challenge as completed.
 *
 * Reward application is best-effort - if it fails after the attempt is
 * already saved, the saved attempt is still returned so the user sees their
 * result; XP/streak will simply be out of sync until their next session.
 */
class CompleteQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
    private val gamificationRepository: GamificationRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val evaluateAchievements: EvaluateAchievementsUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        category: QuizCategory,
        difficulty: QuestionDifficulty,
        answers: List<QuestionAnswer>,
        startedAt: Long,
        isDailyChallenge: Boolean = false,
        challengeId: String? = null,
    ): Resource<QuizAttempt> {
        val score = answers.count { it.isCorrect }
        val totalQuestions = answers.size
        val isPerfectScore = totalQuestions > 0 && score == totalQuestions

        val xpPerCorrectAnswer = (GamificationConfig.BASE_XP_PER_CORRECT_ANSWER * difficulty.xpMultiplier).toLong()
        var xpEarned = score * xpPerCorrectAnswer
        if (totalQuestions > 0) xpEarned += GamificationConfig.QUIZ_COMPLETION_BONUS_XP
        if (isDailyChallenge) xpEarned += GamificationConfig.DAILY_CHALLENGE_BONUS_XP

        var coinsEarned = score.toLong() * GamificationConfig.BASE_COINS_PER_CORRECT_ANSWER
        if (isPerfectScore) coinsEarned += GamificationConfig.PERFECT_SCORE_BONUS_COINS
        if (isDailyChallenge) coinsEarned += GamificationConfig.DAILY_CHALLENGE_BONUS_COINS

        val completedAt = System.currentTimeMillis()
        val attempt = QuizAttempt(
            userId = userId,
            category = category,
            difficulty = difficulty,
            questionIds = answers.map { it.questionId },
            answers = answers,
            score = score,
            totalQuestions = totalQuestions,
            xpEarned = xpEarned,
            coinsEarned = coinsEarned,
            isDailyChallenge = isDailyChallenge,
            challengeId = challengeId,
            startedAt = startedAt,
            completedAt = completedAt,
        )

        val submitResult = quizRepository.submitQuizAttempt(attempt)
        val savedAttempt = (submitResult as? Resource.Success)?.data ?: return submitResult

        gamificationRepository.awardXpAndCoins(userId, xpEarned, coinsEarned)
        val streakResult = gamificationRepository.updateStreak(userId)
        if (isDailyChallenge && challengeId != null) {
            dailyChallengeRepository.markChallengeCompleted(userId, completedAt.toDateId(), savedAttempt.id)
        }
        (streakResult as? Resource.Success)?.data?.let { evaluateAchievements(it) }

        return submitResult
    }
}
