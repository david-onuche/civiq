package com.civiq.app.domain.model

/**
 * Core user profile, combining identity (from Firebase Auth) with civic
 * learning progress (gamification stats). Stored at `users/{uid}` in Firestore.
 */
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.REGISTERED,
    val xp: Long = 0,
    val coins: Long = 0,
    val level: Int = 1,
    val streakCount: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: Long = 0L,
    val countryCode: String? = null,
    val createdAt: Long = 0L,
    val isPremium: Boolean = false,
    val premiumExpiresAt: Long? = null,
    val fcmTokens: List<String> = emptyList(),
) {
    val isGuest: Boolean get() = role == UserRole.GUEST
    val isAdmin: Boolean get() = role == UserRole.ADMIN
}

/**
 * Defines the four CiviQ user roles and the capabilities each unlocks.
 *
 * - [GUEST]: read-only access to a limited preview quiz, no progress saved.
 * - [REGISTERED]: full free-tier access (XP, coins, daily challenges, limited quizzes/day).
 * - [PREMIUM]: unlocks unlimited quizzes, ad-free experience, AI learning coach, exclusive challenges.
 * - [ADMIN]: all premium capabilities plus access to the admin dashboard (manage users/questions/challenges).
 */
enum class UserRole {
    GUEST,
    REGISTERED,
    PREMIUM,
    ADMIN,
}
