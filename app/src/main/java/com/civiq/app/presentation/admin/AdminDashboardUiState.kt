package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.User

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isAccessAllowed: Boolean = false,
    val user: User? = null,
)
