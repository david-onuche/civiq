package com.civiq.app.domain.model

/**
 * A definition of an unlockable achievement/badge. Stored at
 * `achievements/{achievementId}` in Firestore. Which achievements a given
 * user has unlocked is tracked separately via [UserAchievement] in the
 * `users/{uid}/user_achievements` subcollection.
 */
data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "EmojiEvents",
    val category: AchievementCategory = AchievementCategory.MILESTONE,
    val criteriaType: AchievementCriteriaType = AchievementCriteriaType.QUIZZES_COMPLETED,
    val criteriaValue: Int = 0,
    val xpReward: Long = 0,
    val coinReward: Long = 0,
)

enum class AchievementCategory {
    MILESTONE,
    STREAK,
    CATEGORY_MASTERY,
    SOCIAL,
    SPECIAL,
}

/** Determines how [Achievement.criteriaValue] is evaluated against a user's stats. */
enum class AchievementCriteriaType {
    QUIZZES_COMPLETED,
    PERFECT_SCORES,
    STREAK_DAYS,
    XP_EARNED,
    CATEGORY_QUESTIONS_ANSWERED,
    DAILY_CHALLENGES_COMPLETED,
}

/** Records that [achievementId] was unlocked by the current user at [unlockedAt]. */
data class UserAchievement(
    val achievementId: String = "",
    val unlockedAt: Long = 0L,
)

/** Combines an [Achievement] definition with the user's unlock status, for UI rendering. */
data class AchievementWithStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
) {
    val progressFraction: Float
        get() = if (achievement.criteriaValue == 0) {
            0f
        } else {
            (currentProgress.toFloat() / achievement.criteriaValue).coerceIn(0f, 1f)
        }
}
