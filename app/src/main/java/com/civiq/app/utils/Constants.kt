package com.civiq.app.utils

/**
 * Centralized constants for Firestore collection names, DataStore keys,
 * gamification tuning values, and other app-wide configuration.
 *
 * Keeping these in one place avoids "magic string" drift between the
 * data layer (Firestore queries) and documentation (docs/DATABASE.md).
 */
object FirestoreCollections {
    const val USERS = "users"
    const val QUESTIONS = "questions"
    const val QUIZ_ATTEMPTS = "quiz_attempts"
    const val DAILY_CHALLENGES = "daily_challenges"
    const val ACHIEVEMENTS = "achievements"
    const val LEADERBOARDS = "leaderboards"
    const val NOTIFICATIONS = "notifications"
    const val SUBSCRIPTIONS = "subscriptions"
    const val FEATURE_FLAGS = "feature_flags"
    const val REPORTS = "reports"

    /** Subcollection of [USERS]: per-user unlocked achievements. */
    const val USER_ACHIEVEMENTS = "user_achievements"

    /** Subcollection of [USERS]: per-user notification tokens (multi-device FCM). */
    const val USER_DEVICES = "devices"

    /** Subcollection of [LEADERBOARDS]: per-period entries, e.g. leaderboards/{period}/entries. */
    const val LEADERBOARD_ENTRIES = "entries"

    /** Subcollection of [USERS]: per-day daily challenge completion records. */
    const val DAILY_CHALLENGE_PROGRESS = "daily_challenge_progress"
}

object FirestoreFields {
    const val FIELD_XP = "xp"
    const val FIELD_COINS = "coins"
    const val FIELD_LEVEL = "level"
    const val FIELD_STREAK_COUNT = "streakCount"
    const val FIELD_LAST_ACTIVE_DATE = "lastActiveDate"
    const val FIELD_ROLE = "role"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_CATEGORY = "category"
    const val FIELD_DIFFICULTY = "difficulty"
    const val FIELD_DATE = "date"
    const val FIELD_PERIOD = "period"
    const val FIELD_COUNTRY_CODE = "countryCode"
}

object DataStoreKeys {
    const val PREFERENCES_NAME = "civiq_preferences"
    const val KEY_USER_ID = "user_id"
    const val KEY_AUTH_TOKEN_CACHED_AT = "auth_token_cached_at"
    const val KEY_DARK_MODE = "dark_mode_enabled"
    const val KEY_DYNAMIC_COLOR = "dynamic_color_enabled"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
    const val KEY_PREFERRED_COUNTRY_CODE = "preferred_country_code"
    const val KEY_LAST_SYNCED_FCM_TOKEN = "last_synced_fcm_token"
    const val KEY_CACHED_DAILY_CHALLENGE_ID = "cached_daily_challenge_id"
}

/**
 * XP, coin, level, and streak tuning. Centralizing these makes balancing the
 * gamification economy a config change rather than a code hunt.
 */
object GamificationConfig {
    /** Base XP awarded for a correct answer, before difficulty multiplier. */
    const val BASE_XP_PER_CORRECT_ANSWER = 10

    /** Base coins awarded for a correct answer, before difficulty multiplier. */
    const val BASE_COINS_PER_CORRECT_ANSWER = 2

    /** Bonus XP for completing an entire quiz session. */
    const val QUIZ_COMPLETION_BONUS_XP = 25

    /** Bonus coins for completing an entire quiz session with a perfect score. */
    const val PERFECT_SCORE_BONUS_COINS = 50

    /** Bonus XP awarded once per day for completing the daily challenge. */
    const val DAILY_CHALLENGE_BONUS_XP = 50

    /** Bonus coins awarded once per day for completing the daily challenge. */
    const val DAILY_CHALLENGE_BONUS_COINS = 20

    /** XP multiplier applied per [com.civiq.app.domain.model.QuestionDifficulty]. */
    val DIFFICULTY_XP_MULTIPLIER: Map<String, Double> = mapOf(
        "BEGINNER" to 1.0,
        "INTERMEDIATE" to 1.5,
        "ADVANCED" to 2.0,
        "EXPERT" to 3.0,
    )

    /** Number of consecutive active days required to award a streak milestone badge. */
    val STREAK_MILESTONES = listOf(3, 7, 14, 30, 60, 100, 365)

    /** Free-tier daily quiz session limit. Premium users are unlimited. */
    const val FREE_TIER_DAILY_QUIZ_LIMIT = 3

    /** Number of questions per standard quiz session. */
    const val DEFAULT_QUIZ_LENGTH = 10

    /** Number of questions per daily challenge. */
    const val DAILY_CHALLENGE_QUESTION_COUNT = 5
}

/**
 * Cumulative XP thresholds that define [com.civiq.app.domain.model.UserLevel].
 * The level for a given total XP is the highest level whose threshold is <= totalXp.
 */
object LevelThresholds {
    val XP_FOR_LEVEL: Map<Int, Long> = buildMap {
        // Levels 1-50, growing roughly quadratically so later levels take
        // meaningfully longer - a common "RPG curve" used by Duolingo-likes.
        for (level in 1..50) {
            put(level, (100L * level * level))
        }
    }

    fun xpRequiredFor(level: Int): Long = XP_FOR_LEVEL[level] ?: Long.MAX_VALUE

    const val MAX_LEVEL = 50
}

object NetworkConfig {
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 30L
    const val MAX_RETRY_ATTEMPTS = 2
}

object AiConfig {
    const val GEMINI_MODEL = "gemini-1.5-flash"
    const val OPENAI_MODEL = "gpt-4o-mini"
    const val DEFAULT_QUESTION_COUNT = 10
    const val MAX_QUESTION_COUNT = 25
    const val DEFAULT_TEMPERATURE = 0.9
}

object NotificationChannels {
    const val GENERAL_CHANNEL_ID = "civiq_general_channel"
    const val DAILY_QUIZ_CHANNEL_ID = "civiq_daily_quiz_channel"
    const val STREAK_CHANNEL_ID = "civiq_streak_channel"
    const val ACHIEVEMENT_CHANNEL_ID = "civiq_achievement_channel"
    const val WEEKLY_CHALLENGE_CHANNEL_ID = "civiq_weekly_challenge_channel"
}
