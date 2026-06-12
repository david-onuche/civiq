package com.civiq.app.presentation.premium

import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.User
import com.civiq.app.utils.UiText

/** UI state for [PremiumScreen][com.civiq.app.presentation.premium.PremiumScreen]. */
data class PremiumUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val subscription: Subscription? = null,
    val isProcessing: Boolean = false,
    val errorMessage: UiText? = null,
)
