package com.civiq.app.domain.repository

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserAchievement
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** XP, coins, levels, streaks, badges, and achievements. */
interface GamificationRepository {

    /** Atomically increments the user's XP and coin balances and recalculates [User.level]. */
    suspend fun awardXpAndCoins(userId: String, xp: Long, coins: Long): Resource<User>

    /**
     * Updates the user's streak based on [User.lastActiveDate]:
     * - same day -> no change
     * - exactly one day later -> increment streak
     * - more than one day later -> reset streak to 1
     */
    suspend fun updateStreak(userId: String): Resource<User>

    /** All achievement definitions (`achievements` collection), for rendering progress UI. */
    fun observeAchievements(): Flow<Resource<List<Achievement>>>

    /** Achievements the user has unlocked (`users/{uid}/user_achievements`). */
    fun observeUserAchievements(userId: String): Flow<Resource<List<UserAchievement>>>

    suspend fun unlockAchievement(userId: String, achievementId: String): Resource<UserAchievement>
}
