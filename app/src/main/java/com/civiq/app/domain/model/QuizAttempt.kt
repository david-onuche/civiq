package com.civiq.app.domain.model

/**
 * A completed (or in-progress) quiz session. Stored at
 * `quiz_attempts/{attemptId}` in Firestore, and surfaced on the Profile
 * screen as "Quiz History".
 */
data class QuizAttempt(
    val id: String = "",
    val userId: String = "",
    val category: QuizCategory = QuizCategory.DEMOCRACY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.BEGINNER,
    val questionIds: List<String> = emptyList(),
    val answers: List<QuestionAnswer> = emptyList(),
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Long = 0,
    val coinsEarned: Long = 0,
    val isDailyChallenge: Boolean = false,
    val challengeId: String? = null,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
) {
    val accuracy: Float
        get() = if (totalQuestions == 0) 0f else score.toFloat() / totalQuestions

    val isPerfectScore: Boolean
        get() = totalQuestions > 0 && score == totalQuestions
}

/** Records the user's answer to a single question within a [QuizAttempt]. */
data class QuestionAnswer(
    val questionId: String = "",
    val selectedIndex: Int = -1,
    val isCorrect: Boolean = false,
    val timeTakenMs: Long = 0L,
)
