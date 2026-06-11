package com.civiq.app.domain.model

import com.civiq.app.utils.GamificationConfig

/**
 * A single day's featured civic mission. Stored at
 * `daily_challenges/{yyyy-MM-dd}` in Firestore so the document ID doubles
 * as the date key.
 */
data class DailyChallenge(
    val id: String = "",
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val category: QuizCategory = QuizCategory.CIVIC_RESPONSIBILITY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.INTERMEDIATE,
    val questionIds: List<String> = emptyList(),
    val xpReward: Long = GamificationConfig.DAILY_CHALLENGE_BONUS_XP.toLong(),
    val coinReward: Long = GamificationConfig.DAILY_CHALLENGE_BONUS_COINS.toLong(),
    val createdAt: Long = 0L,
)

/**
 * Per-user completion record for a [DailyChallenge], stored at
 * `users/{uid}/daily_challenge_progress/{date}`.
 */
data class DailyChallengeProgress(
    val challengeId: String = "",
    val userId: String = "",
    val isCompleted: Boolean = false,
    val attemptId: String? = null,
    val completedAt: Long? = null,
)
