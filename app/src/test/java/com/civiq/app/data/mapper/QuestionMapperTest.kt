package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.QuestionDto
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionSource
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuestionMapperTest {

    private val dto = QuestionDto(
        id = "q-1",
        type = "SCENARIO",
        category = "ELECTIONS",
        difficulty = "ADVANCED",
        questionText = "What should a poll worker do?",
        options = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 2,
        explanation = "Because of electoral law.",
        tone = "SATIRICAL",
        countryCode = "NG",
        tags = listOf("voting", "ethics"),
        source = "AI_GENERATED",
        createdAt = 1_700_000_000_000L,
        createdBy = "admin-uid",
    )

    @Test
    fun `toDomain maps all enum and scalar fields`() {
        val domain = dto.toDomain()

        assertThat(domain).isEqualTo(
            Question(
                id = "q-1",
                type = QuestionType.SCENARIO,
                category = QuizCategory.ELECTIONS,
                difficulty = QuestionDifficulty.ADVANCED,
                questionText = "What should a poll worker do?",
                options = listOf("A", "B", "C", "D"),
                correctAnswerIndex = 2,
                explanation = "Because of electoral law.",
                tone = QuestionTone.SATIRICAL,
                countryCode = "NG",
                tags = listOf("voting", "ethics"),
                source = QuestionSource.AI_GENERATED,
                createdAt = 1_700_000_000_000L,
                createdBy = "admin-uid",
            ),
        )
    }

    @Test
    fun `toDomain falls back to defaults for unrecognized enum strings`() {
        val malformed = QuestionDto(
            type = "UNKNOWN_TYPE",
            category = "UNKNOWN_CATEGORY",
            difficulty = "UNKNOWN_DIFFICULTY",
            tone = "UNKNOWN_TONE",
            source = "UNKNOWN_SOURCE",
        )

        val domain = malformed.toDomain()

        assertThat(domain.type).isEqualTo(QuestionType.MULTIPLE_CHOICE)
        assertThat(domain.category).isEqualTo(QuizCategory.DEMOCRACY)
        assertThat(domain.difficulty).isEqualTo(QuestionDifficulty.BEGINNER)
        assertThat(domain.tone).isEqualTo(QuestionTone.EDUCATIONAL)
        assertThat(domain.source).isEqualTo(QuestionSource.CURATED)
    }

    @Test
    fun `toDto round-trips back to an equal domain Question`() {
        val domain = dto.toDomain()

        assertThat(domain.toDto()).isEqualTo(dto)
        assertThat(domain.toDto().toDomain()).isEqualTo(domain)
    }
}
