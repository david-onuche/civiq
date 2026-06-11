package com.civiq.app.presentation.auth

/** User-driven actions handled by [AuthViewModel]. */
sealed interface AuthUiEvent {
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data class ConfirmPasswordChanged(val value: String) : AuthUiEvent
    data class DisplayNameChanged(val value: String) : AuthUiEvent
    data object SignIn : AuthUiEvent
    data object Register : AuthUiEvent
    data object ContinueAsGuest : AuthUiEvent
    data class SignInWithGoogle(val idToken: String) : AuthUiEvent
    data object SendPasswordResetEmail : AuthUiEvent
    data object SignOut : AuthUiEvent
    data object ErrorShown : AuthUiEvent
}
