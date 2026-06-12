package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.User

/**
 * Snapshot of the metrics referenced by [AchievementCriteriaType], derived
 * from a user's profile and quiz history. Used to compute progress toward
 * locked achievements and to evaluate which ones a user newly qualifies for.
 */
data class UserProgressStats(
    val xp: Long,
    val streakDays: Int,
    val quizzesCompleted: Int,
    val perfectScores: Int,
    val questionsAnswered: Int,
    val dailyChallengesCompleted: Int,
)

/** Builds [UserProgressStats] from a user's profile fields and their quiz attempt history. */
fun buildUserProgressStats(user: User, attempts: List<QuizAttempt>): UserProgressStats = UserProgressStats(
    xp = user.xp,
    streakDays = user.streakCount,
    quizzesCompleted = attempts.size,
    perfectScores = attempts.count { it.isPerfectScore },
    questionsAnswered = attempts.sumOf { it.totalQuestions },
    dailyChallengesCompleted = attempts.count { it.isDailyChallenge },
)

/** Reads the value of [this] corresponding to [criteriaType], for comparison against [com.civiq.app.domain.model.Achievement.criteriaValue]. */
fun UserProgressStats.progressValue(criteriaType: AchievementCriteriaType): Int = when (criteriaType) {
    AchievementCriteriaType.QUIZZES_COMPLETED -> quizzesCompleted
    AchievementCriteriaType.PERFECT_SCORES -> perfectScores
    AchievementCriteriaType.STREAK_DAYS -> streakDays
    AchievementCriteriaType.XP_EARNED -> xp.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    AchievementCriteriaType.CATEGORY_QUESTIONS_ANSWERED -> questionsAnswered
    AchievementCriteriaType.DAILY_CHALLENGES_COMPLETED -> dailyChallengesCompleted
}
