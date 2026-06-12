package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Checks every achievement definition against [user]'s latest stats and
 * unlocks any that have newly become eligible, awarding their XP/coin
 * rewards. Called after [com.civiq.app.domain.usecase.quiz.CompleteQuizUseCase]
 * applies a quiz attempt's own rewards, so [user] should reflect the
 * post-attempt XP/streak values.
 */
class EvaluateAchievementsUseCase @Inject constructor(
    private val gamificationRepository: GamificationRepository,
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(user: User) {
        val achievements = (gamificationRepository.observeAchievements().first() as? Resource.Success)?.data
            ?: return
        if (achievements.isEmpty()) return

        val unlocked = (gamificationRepository.observeUserAchievements(user.id).first() as? Resource.Success)?.data
            ?: return
        val unlockedIds = unlocked.map { it.achievementId }.toSet()
        val locked = achievements.filterNot { it.id in unlockedIds }
        if (locked.isEmpty()) return

        val attempts = (quizRepository.observeQuizHistory(user.id, limit = HISTORY_LIMIT).first() as? Resource.Success)
            ?.data
            .orEmpty()
        val stats = buildUserProgressStats(user, attempts)

        for (achievement in locked) {
            if (stats.progressValue(achievement.criteriaType) >= achievement.criteriaValue) {
                gamificationRepository.unlockAchievement(user.id, achievement.id)
                if (achievement.xpReward > 0 || achievement.coinReward > 0) {
                    gamificationRepository.awardXpAndCoins(user.id, achievement.xpReward, achievement.coinReward)
                }
            }
        }
    }

    private companion object {
        /** High enough that quiz-count-based achievement criteria see a user's full history. */
        const val HISTORY_LIMIT = 500
    }
}
