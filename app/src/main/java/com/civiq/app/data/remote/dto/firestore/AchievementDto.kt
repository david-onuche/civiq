package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `achievements/{achievementId}`. See docs/DATABASE.md. */
data class AchievementDto(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "EmojiEvents",
    val category: String = "MILESTONE",
    val criteriaType: String = "QUIZZES_COMPLETED",
    val criteriaValue: Int = 0,
    val xpReward: Long = 0,
    val coinReward: Long = 0,
)

/** Firestore document shape for `users/{uid}/user_achievements/{achievementId}`. */
data class UserAchievementDto(
    @DocumentId val achievementId: String = "",
    val unlockedAt: Long = 0L,
)
