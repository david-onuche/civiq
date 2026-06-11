package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.DailyChallengeDto
import com.civiq.app.data.remote.dto.firestore.DailyChallengeProgressDto
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.safeEnumValueOf

fun DailyChallengeDto.toDomain(): DailyChallenge = DailyChallenge(
    id = id,
    date = date,
    title = title,
    description = description,
    category = safeEnumValueOf(category, QuizCategory.CIVIC_RESPONSIBILITY),
    difficulty = safeEnumValueOf(difficulty, QuestionDifficulty.INTERMEDIATE),
    questionIds = questionIds,
    xpReward = xpReward,
    coinReward = coinReward,
    createdAt = createdAt,
)

fun DailyChallenge.toDto(): DailyChallengeDto = DailyChallengeDto(
    id = id,
    date = date,
    title = title,
    description = description,
    category = category.name,
    difficulty = difficulty.name,
    questionIds = questionIds,
    xpReward = xpReward,
    coinReward = coinReward,
    createdAt = createdAt,
)

fun DailyChallengeProgressDto.toDomain(): DailyChallengeProgress = DailyChallengeProgress(
    challengeId = challengeId,
    userId = userId,
    isCompleted = isCompleted,
    attemptId = attemptId,
    completedAt = completedAt,
)

fun DailyChallengeProgress.toDto(): DailyChallengeProgressDto = DailyChallengeProgressDto(
    challengeId = challengeId,
    userId = userId,
    isCompleted = isCompleted,
    attemptId = attemptId,
    completedAt = completedAt,
)
