package com.civiq.app.presentation.auth

import com.civiq.app.utils.UiText

/** UI state shared by [com.civiq.app.presentation.auth.LoginScreen], [RegisterScreen], and [ForgotPasswordScreen]. */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isSignedOut: Boolean = false,
    val passwordResetEmailSent: Boolean = false,
    val errorMessage: UiText? = null,
)
