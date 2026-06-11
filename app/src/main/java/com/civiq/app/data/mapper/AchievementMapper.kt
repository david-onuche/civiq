package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.AchievementDto
import com.civiq.app.data.remote.dto.firestore.UserAchievementDto
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCategory
import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.domain.model.UserAchievement
import com.civiq.app.utils.safeEnumValueOf

fun AchievementDto.toDomain(): Achievement = Achievement(
    id = id,
    title = title,
    description = description,
    iconName = iconName,
    category = safeEnumValueOf(category, AchievementCategory.MILESTONE),
    criteriaType = safeEnumValueOf(criteriaType, AchievementCriteriaType.QUIZZES_COMPLETED),
    criteriaValue = criteriaValue,
    xpReward = xpReward,
    coinReward = coinReward,
)

fun Achievement.toDto(): AchievementDto = AchievementDto(
    id = id,
    title = title,
    description = description,
    iconName = iconName,
    category = category.name,
    criteriaType = criteriaType.name,
    criteriaValue = criteriaValue,
    xpReward = xpReward,
    coinReward = coinReward,
)

fun UserAchievementDto.toDomain(): UserAchievement = UserAchievement(
    achievementId = achievementId,
    unlockedAt = unlockedAt,
)

fun UserAchievement.toDto(): UserAchievementDto = UserAchievementDto(
    achievementId = achievementId,
    unlockedAt = unlockedAt,
)
