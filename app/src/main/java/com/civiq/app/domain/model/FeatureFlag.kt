package com.civiq.app.domain.model

/**
 * A remotely-configurable feature toggle, backed by Firestore
 * (`feature_flags/{key}`) with [com.google.firebase.remoteconfig.FirebaseRemoteConfig]
 * as a low-latency fallback/cache.
 */
data class FeatureFlag(
    val key: String = "",
    val isEnabled: Boolean = false,
    val description: String = "",
    val requiresPremium: Boolean = false,
)

/** Well-known feature flag keys used by the Premium architecture. */
object FeatureFlagKeys {
    const val UNLIMITED_QUIZZES = "unlimited_quizzes"
    const val AD_FREE_EXPERIENCE = "ad_free_experience"
    const val AI_LEARNING_COACH = "ai_learning_coach"
    const val EXCLUSIVE_CHALLENGES = "exclusive_challenges"

    /** All premium-gated feature keys, used to seed Firestore and the admin dashboard. */
    val PREMIUM_FEATURES = listOf(
        UNLIMITED_QUIZZES,
        AD_FREE_EXPERIENCE,
        AI_LEARNING_COACH,
        EXCLUSIVE_CHALLENGES,
    )
}
