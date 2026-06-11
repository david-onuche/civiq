package com.civiq.app.domain.model

import com.civiq.app.utils.LevelThresholds
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a user's current level, civic rank title, and progress toward
 * the next level - shown on the Home and Profile screens as an XP progress bar.
 */
data class UserLevel(
    val level: Int,
    val title: String,
    val currentXp: Long,
    val xpForCurrentLevel: Long,
    val xpForNextLevel: Long,
) {
    /** Progress (0f..1f) toward the next level, used to fill the XP progress bar. */
    val progress: Float
        get() {
            if (level >= LevelThresholds.MAX_LEVEL) return 1f
            val span = (xpForNextLevel - xpForCurrentLevel).toFloat()
            if (span <= 0f) return 1f
            val into = (currentXp - xpForCurrentLevel).toFloat()
            return min(1f, max(0f, into / span))
        }

    val xpRemainingForNextLevel: Long
        get() = max(0L, xpForNextLevel - currentXp)
}

/**
 * Civic rank titles awarded at specific levels. The title shown for any
 * level is the highest-threshold title at or below that level (see
 * [UserLevels.titleForLevel]).
 */
object UserLevels {

    val TITLES: Map<Int, String> = mapOf(
        1 to "Civic Rookie",
        5 to "Democracy Explorer",
        10 to "Policy Analyst",
        15 to "Constitution Scholar",
        20 to "Governance Expert",
        25 to "Diplomacy Strategist",
        30 to "Civic Champion",
        40 to "Civic Luminary",
        50 to "Founding Citizen",
    )

    /** Returns the rank title for [level], falling back to the closest lower threshold. */
    fun titleForLevel(level: Int): String {
        val applicableLevel = TITLES.keys
            .filter { it <= level }
            .maxOrNull() ?: 1
        return TITLES.getValue(applicableLevel)
    }

    /** Computes the [UserLevel] (level, title, progress) for a given total XP balance. */
    fun fromTotalXp(totalXp: Long): UserLevel {
        var level = 1
        for (candidate in 1..LevelThresholds.MAX_LEVEL) {
            if (totalXp >= LevelThresholds.xpRequiredFor(candidate)) {
                level = candidate
            } else {
                break
            }
        }
        val xpForCurrent = if (level == 1) 0L else LevelThresholds.xpRequiredFor(level)
        val xpForNext = LevelThresholds.xpRequiredFor(level + 1)
        return UserLevel(
            level = level,
            title = titleForLevel(level),
            currentXp = totalXp,
            xpForCurrentLevel = xpForCurrent,
            xpForNextLevel = xpForNext,
        )
    }
}
