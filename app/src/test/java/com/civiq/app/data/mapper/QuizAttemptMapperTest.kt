package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.QuestionAnswerDto
import com.civiq.app.data.remote.dto.firestore.QuizAttemptDto
import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuizAttemptMapperTest {

    private val dto = QuizAttemptDto(
        id = "attempt-1",
        userId = "user-1",
        category = "GOVERNANCE",
        difficulty = "EXPERT",
        questionIds = listOf("q1", "q2", "q3"),
        answers = listOf(
            QuestionAnswerDto(questionId = "q1", selectedIndex = 1, isCorrect = true, timeTakenMs = 5_000L),
            QuestionAnswerDto(questionId = "q2", selectedIndex = 0, isCorrect = false, timeTakenMs = 8_000L),
            QuestionAnswerDto(questionId = "q3", selectedIndex = 2, isCorrect = true, timeTakenMs = 3_000L),
        ),
        score = 2,
        totalQuestions = 3,
        xpEarned = 85,
        coinsEarned = 14,
        isDailyChallenge = true,
        challengeId = "2026-06-12",
        startedAt = 1_700_000_000_000L,
        completedAt = 1_700_000_060_000L,
    )

    @Test
    fun `toDomain maps attempt fields, enums, and embedded answers`() {
        val domain = dto.toDomain()

        assertThat(domain).isEqualTo(
            QuizAttempt(
                id = "attempt-1",
                userId = "user-1",
                category = QuizCategory.GOVERNANCE,
                difficulty = QuestionDifficulty.EXPERT,
                questionIds = listOf("q1", "q2", "q3"),
                answers = listOf(
                    QuestionAnswer("q1", 1, true, 5_000L),
                    QuestionAnswer("q2", 0, false, 8_000L),
                    QuestionAnswer("q3", 2, true, 3_000L),
                ),
                score = 2,
                totalQuestions = 3,
                xpEarned = 85,
                coinsEarned = 14,
                isDailyChallenge = true,
                challengeId = "2026-06-12",
                startedAt = 1_700_000_000_000L,
                completedAt = 1_700_000_060_000L,
            ),
        )
    }

    @Test
    fun `toDomain computes accuracy and perfect-score flags from mapped score`() {
        val domain = dto.toDomain()

        assertThat(domain.accuracy).isWithin(0.001f).of(2f / 3f)
        assertThat(domain.isPerfectScore).isFalse()
    }

    @Test
    fun `toDto round-trips back to an equal DTO`() {
        val domain = dto.toDomain()

        assertThat(domain.toDto()).isEqualTo(dto)
    }

    @Test
    fun `unrecognized category and difficulty strings fall back to defaults`() {
        val malformed = dto.copy(category = "NOT_A_CATEGORY", difficulty = "NOT_A_DIFFICULTY")

        val domain = malformed.toDomain()

        assertThat(domain.category).isEqualTo(QuizCategory.DEMOCRACY)
        assertThat(domain.difficulty).isEqualTo(QuestionDifficulty.BEGINNER)
    }
}
