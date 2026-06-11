package com.civiq.app.domain.model

/**
 * A single ranked row on a leaderboard. Stored at
 * `leaderboards/{period}_{scope}/entries/{userId}` and recomputed
 * periodically by a Cloud Function.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val xp: Long = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val countryCode: String? = null,
)

/** Time window a leaderboard covers. */
enum class LeaderboardPeriod(val displayName: String) {
    DAILY("Today"),
    WEEKLY("This Week"),
    MONTHLY("This Month"),
    ALL_TIME("All Time"),
}

/** Population a leaderboard ranks against. */
enum class LeaderboardScope(val displayName: String) {
    GLOBAL("Global"),
    COUNTRY("My Country"),
    FRIENDS("Friends"),
}
