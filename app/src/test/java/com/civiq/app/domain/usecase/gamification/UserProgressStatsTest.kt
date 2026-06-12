package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.User
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserProgressStatsTest {

    private val user = User(id = "u1", xp = 1_250, streakCount = 4)

    private fun attempt(
        score: Int,
        totalQuestions: Int,
        isDailyChallenge: Boolean = false,
    ): QuizAttempt = QuizAttempt(
        userId = "u1",
        score = score,
        totalQuestions = totalQuestions,
        answers = List(totalQuestions) { index ->
            QuestionAnswer(questionId = "q$index", selectedIndex = 0, isCorrect = index < score)
        },
        isDailyChallenge = isDailyChallenge,
    )

    @Test
    fun `buildUserProgressStats aggregates xp, streak, and quiz history`() {
        val attempts = listOf(
            attempt(score = 5, totalQuestions = 5, isDailyChallenge = true), // perfect, daily challenge
            attempt(score = 3, totalQuestions = 5),
            attempt(score = 5, totalQuestions = 5), // perfect
        )

        val stats = buildUserProgressStats(user, attempts)

        assertThat(stats).isEqualTo(
            UserProgressStats(
                xp = 1_250,
                streakDays = 4,
                quizzesCompleted = 3,
                perfectScores = 2,
                questionsAnswered = 15,
                dailyChallengesCompleted = 1,
            ),
        )
    }

    @Test
    fun `buildUserProgressStats handles a user with no quiz history`() {
        val stats = buildUserProgressStats(user, emptyList())

        assertThat(stats).isEqualTo(
            UserProgressStats(
                xp = 1_250,
                streakDays = 4,
                quizzesCompleted = 0,
                perfectScores = 0,
                questionsAnswered = 0,
                dailyChallengesCompleted = 0,
            ),
        )
    }

    @Test
    fun `progressValue reads the field matching each criteria type`() {
        val stats = UserProgressStats(
            xp = 5_000,
            streakDays = 10,
            quizzesCompleted = 42,
            perfectScores = 8,
            questionsAnswered = 210,
            dailyChallengesCompleted = 12,
        )

        assertThat(stats.progressValue(AchievementCriteriaType.QUIZZES_COMPLETED)).isEqualTo(42)
        assertThat(stats.progressValue(AchievementCriteriaType.PERFECT_SCORES)).isEqualTo(8)
        assertThat(stats.progressValue(AchievementCriteriaType.STREAK_DAYS)).isEqualTo(10)
        assertThat(stats.progressValue(AchievementCriteriaType.XP_EARNED)).isEqualTo(5_000)
        assertThat(stats.progressValue(AchievementCriteriaType.CATEGORY_QUESTIONS_ANSWERED)).isEqualTo(210)
        assertThat(stats.progressValue(AchievementCriteriaType.DAILY_CHALLENGES_COMPLETED)).isEqualTo(12)
    }

    @Test
    fun `progressValue caps xp at Int MAX_VALUE`() {
        val stats = UserProgressStats(
            xp = Long.MAX_VALUE,
            streakDays = 0,
            quizzesCompleted = 0,
            perfectScores = 0,
            questionsAnswered = 0,
            dailyChallengesCompleted = 0,
        )

        assertThat(stats.progressValue(AchievementCriteriaType.XP_EARNED)).isEqualTo(Int.MAX_VALUE)
    }
}
