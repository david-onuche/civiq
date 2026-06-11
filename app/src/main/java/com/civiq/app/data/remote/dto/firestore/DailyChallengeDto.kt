package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `daily_challenges/{yyyy-MM-dd}`. See docs/DATABASE.md. */
data class DailyChallengeDto(
    @DocumentId val id: String = "",
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "CIVIC_RESPONSIBILITY",
    val difficulty: String = "INTERMEDIATE",
    val questionIds: List<String> = emptyList(),
    val xpReward: Long = 0,
    val coinReward: Long = 0,
    val createdAt: Long = 0L,
)

/** Firestore document shape for `users/{uid}/daily_challenge_progress/{date}`. */
data class DailyChallengeProgressDto(
    @DocumentId val challengeId: String = "",
    val userId: String = "",
    val isCompleted: Boolean = false,
    val attemptId: String? = null,
    val completedAt: Long? = null,
)
