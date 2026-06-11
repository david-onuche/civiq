package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `leaderboards/{period}_{scope}/entries/{userId}`. See docs/DATABASE.md. */
data class LeaderboardEntryDto(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val xp: Long = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val countryCode: String? = null,
)
