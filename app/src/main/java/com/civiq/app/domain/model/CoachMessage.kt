package com.civiq.app.domain.model

/** A single turn in an AI Learning Coach conversation (see [com.civiq.app.domain.repository.AiCoachRepository]). */
data class CoachMessage(
    val role: CoachMessageRole,
    val content: String,
)

enum class CoachMessageRole {
    USER,
    COACH,
}
