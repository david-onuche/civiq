package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines achievement definitions, the user's unlock records, and their
 * quiz history into the full [AchievementWithStatus] list - including
 * progress toward not-yet-unlocked achievements - for the Achievements screen.
 */
class ObserveAchievementsWithStatusUseCase @Inject constructor(
    private val gamificationRepository: GamificationRepository,
    private val quizRepository: QuizRepository,
) {
    operator fun invoke(user: User): Flow<Resource<List<AchievementWithStatus>>> =
        combine(
            gamificationRepository.observeAchievements(),
            gamificationRepository.observeUserAchievements(user.id),
            quizRepository.observeQuizHistory(user.id, limit = HISTORY_LIMIT),
        ) { achievementsResource, userAchievementsResource, historyResource ->
            when {
                achievementsResource is Resource.Error -> Resource.Error(achievementsResource.message)
                userAchievementsResource is Resource.Error -> Resource.Error(userAchievementsResource.message)
                achievementsResource is Resource.Success && userAchievementsResource is Resource.Success -> {
                    val unlockedById = userAchievementsResource.data.associateBy { it.achievementId }
                    val attempts = (historyResource as? Resource.Success)?.data.orEmpty()
                    val stats = buildUserProgressStats(user, attempts)
                    val items = achievementsResource.data
                        .map { achievement ->
                            val record = unlockedById[achievement.id]
                            AchievementWithStatus(
                                achievement = achievement,
                                isUnlocked = record != null,
                                unlockedAt = record?.unlockedAt,
                                currentProgress = if (record != null) {
                                    achievement.criteriaValue
                                } else {
                                    stats.progressValue(achievement.criteriaType)
                                },
                            )
                        }
                        .sortedWith(compareBy({ !it.isUnlocked }, { it.achievement.title }))
                    Resource.Success(items)
                }
                else -> Resource.Loading()
            }
        }

    private companion object {
        /** High enough that quiz-count-based achievement criteria see a user's full history. */
        const val HISTORY_LIMIT = 500
    }
}
