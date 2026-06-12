package com.civiq.app.domain.repository

import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.utils.Resource

/**
 * Abstraction over AI chat providers (Gemini, OpenAI) powering CiviQ's
 * Premium "AI Learning Coach". Implementations should attempt the configured
 * primary provider and fall back to a secondary provider on failure.
 */
interface AiCoachRepository {
    suspend fun sendMessage(history: List<CoachMessage>): Resource<String>
}
