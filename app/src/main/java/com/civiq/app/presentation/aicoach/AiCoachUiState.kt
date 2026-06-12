package com.civiq.app.presentation.aicoach

import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.utils.UiText

/** UI state for [AiCoachScreen][com.civiq.app.presentation.aicoach.AiCoachScreen]. */
data class AiCoachUiState(
    val isLoading: Boolean = true,
    val isAccessAllowed: Boolean = false,
    val messages: List<CoachMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: UiText? = null,
)
