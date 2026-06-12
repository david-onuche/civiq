package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.AchievementDto
import com.civiq.app.data.remote.dto.firestore.UserAchievementDto
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCategory
import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.domain.model.UserAchievement
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AchievementMapperTest {

    @Test
    fun `AchievementDto toDomain maps category and criteria enums`() {
        val dto = AchievementDto(
            id = "ach-1",
            title = "Streak Master",
            description = "Maintain a 7-day streak",
            iconName = "LocalFireDepartment",
            category = "STREAK",
            criteriaType = "STREAK_DAYS",
            criteriaValue = 7,
            xpReward = 100,
            coinReward = 25,
        )

        val domain = dto.toDomain()

        assertThat(domain).isEqualTo(
            Achievement(
                id = "ach-1",
                title = "Streak Master",
                description = "Maintain a 7-day streak",
                iconName = "LocalFireDepartment",
                category = AchievementCategory.STREAK,
                criteriaType = AchievementCriteriaType.STREAK_DAYS,
                criteriaValue = 7,
                xpReward = 100,
                coinReward = 25,
            ),
        )
    }

    @Test
    fun `AchievementDto toDomain falls back to defaults for unrecognized enum strings`() {
        val dto = AchievementDto(category = "NOT_A_CATEGORY", criteriaType = "NOT_A_CRITERIA")

        val domain = dto.toDomain()

        assertThat(domain.category).isEqualTo(AchievementCategory.MILESTONE)
        assertThat(domain.criteriaType).isEqualTo(AchievementCriteriaType.QUIZZES_COMPLETED)
    }

    @Test
    fun `Achievement toDto round-trips back to an equal domain model`() {
        val achievement = Achievement(
            id = "ach-2",
            title = "Perfectionist",
            description = "Score 100% on a quiz",
            category = AchievementCategory.CATEGORY_MASTERY,
            criteriaType = AchievementCriteriaType.PERFECT_SCORES,
            criteriaValue = 1,
            xpReward = 50,
            coinReward = 10,
        )

        val dto = achievement.toDto()

        assertThat(dto.category).isEqualTo("CATEGORY_MASTERY")
        assertThat(dto.criteriaType).isEqualTo("PERFECT_SCORES")
        assertThat(dto.toDomain()).isEqualTo(achievement)
    }

    @Test
    fun `UserAchievement mapper round-trips id and unlock timestamp`() {
        val dto = UserAchievementDto(achievementId = "ach-3", unlockedAt = 1_700_000_000_000L)

        val domain = dto.toDomain()

        assertThat(domain).isEqualTo(UserAchievement(achievementId = "ach-3", unlockedAt = 1_700_000_000_000L))
        assertThat(domain.toDto()).isEqualTo(dto)
    }
}
