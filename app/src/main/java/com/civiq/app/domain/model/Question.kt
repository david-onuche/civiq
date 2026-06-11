package com.civiq.app.domain.model

/**
 * A single quiz question. Stored at `questions/{questionId}` in Firestore
 * and also used as the in-memory representation for AI-generated questions
 * before they are persisted (see `AiQuizRepository`).
 *
 * For [QuestionType.TRUE_FALSE], [options] is `["True", "False"]` and
 * [correctAnswerIndex] is 0 or 1. For [QuestionType.SCENARIO], the question
 * is framed as a short situation followed by multiple-choice responses.
 */
data class Question(
    val id: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val category: QuizCategory = QuizCategory.DEMOCRACY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.BEGINNER,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "",
    val tone: QuestionTone = QuestionTone.EDUCATIONAL,
    val countryCode: String? = null,
    val tags: List<String> = emptyList(),
    val source: QuestionSource = QuestionSource.CURATED,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
) {
    fun isCorrectAnswer(selectedIndex: Int): Boolean = selectedIndex == correctAnswerIndex
}
