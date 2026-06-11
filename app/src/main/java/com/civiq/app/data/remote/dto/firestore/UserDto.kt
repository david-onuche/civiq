package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `users/{uid}`. See docs/DATABASE.md. */
data class UserDto(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: String = "REGISTERED",
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
)
