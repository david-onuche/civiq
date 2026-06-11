package com.civiq.app.domain.usecase.home

import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines achievement definitions with the user's unlock records into the
 * most recently unlocked [AchievementWithStatus] entries, for the Home
 * screen's "Recent Achievements" section.
 */
class ObserveRecentAchievementsUseCase @Inject constructor(
    private val gamificationRepository: GamificationRepository,
) {
    operator fun invoke(userId: String, limit: Int = 5): Flow<Resource<List<AchievementWithStatus>>> =
        combine(
            gamificationRepository.observeAchievements(),
            gamificationRepository.observeUserAchievements(userId),
        ) { achievementsResource, userAchievementsResource ->
            when {
                achievementsResource is Resource.Error ->
                    Resource.Error(achievementsResource.message)

                userAchievementsResource is Resource.Error ->
                    Resource.Error(userAchievementsResource.message)

                achievementsResource is Resource.Success && userAchievementsResource is Resource.Success -> {
                    val unlockedById = userAchievementsResource.data.associateBy { it.achievementId }
                    val unlocked = achievementsResource.data
                        .mapNotNull { achievement ->
                            val record = unlockedById[achievement.id] ?: return@mapNotNull null
                            AchievementWithStatus(
                                achievement = achievement,
                                isUnlocked = true,
                                unlockedAt = record.unlockedAt,
                                currentProgress = achievement.criteriaValue,
                            )
                        }
                        .sortedByDescending { it.unlockedAt }
                        .take(limit)
                    Resource.Success(unlocked)
                }

                else -> Resource.Loading()
            }
        }
}
