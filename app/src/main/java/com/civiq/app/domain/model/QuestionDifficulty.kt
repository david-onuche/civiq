package com.civiq.app.domain.model

import com.civiq.app.utils.GamificationConfig

/** The four difficulty tiers used across quiz content, AI generation, and the admin editor. */
enum class QuestionDifficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    /** XP/coin multiplier applied when scoring a correct answer at this difficulty. */
    val xpMultiplier: Double
        get() = GamificationConfig.DIFFICULTY_XP_MULTIPLIER[name] ?: 1.0
}

/** Question format. Scenario questions present a short civic situation followed by a judgment call. */
enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SCENARIO,
}

/** The "voice" of a question - powers the Humor & Satire mode alongside straight education. */
enum class QuestionTone {
    EDUCATIONAL,
    FUNNY,
    SATIRICAL,
}

/** Where a question originated, used for moderation and analytics. */
enum class QuestionSource {
    CURATED,
    AI_GENERATED,
    COMMUNITY,
}
