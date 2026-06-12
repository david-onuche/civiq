package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.utils.UiText

data class AdminUserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val selectedRole: UserRole? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: UiText? = null,
) {
    val hasUnsavedChanges: Boolean
        get() = user != null && selectedRole != null && selectedRole != user.role
}
