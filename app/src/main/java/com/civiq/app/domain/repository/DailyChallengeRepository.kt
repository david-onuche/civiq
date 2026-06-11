package com.civiq.app.domain.repository

import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** Today's featured civic mission and the current user's completion status for it. */
interface DailyChallengeRepository {

    /** Fetches `daily_challenges/{today}`. If missing, an AI-generated challenge is created and persisted. */
    suspend fun getTodayChallenge(): Resource<DailyChallenge>

    fun observeChallengeProgress(userId: String, date: String): Flow<Resource<DailyChallengeProgress?>>

    suspend fun markChallengeCompleted(userId: String, date: String, attemptId: String): Resource<Unit>
}
