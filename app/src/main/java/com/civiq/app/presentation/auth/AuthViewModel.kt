package com.civiq.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.User
import com.civiq.app.domain.usecase.auth.AuthUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Login, Register, and Forgot Password screens. A fresh instance is
 * created per screen via `hiltViewModel()`, but all three share the same
 * [AuthUiState]/[AuthUiEvent] contract since they manipulate overlapping fields
 * (email is used by all three, password by Login/Register, etc.).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, errorMessage = null) }

            is AuthUiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, errorMessage = null) }

            is AuthUiEvent.ConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.value, errorMessage = null) }

            is AuthUiEvent.DisplayNameChanged ->
                _uiState.update { it.copy(displayName = event.value, errorMessage = null) }

            AuthUiEvent.SignIn -> signIn()
            AuthUiEvent.Register -> register()
            AuthUiEvent.ContinueAsGuest -> continueAsGuest()
            is AuthUiEvent.SignInWithGoogle -> signInWithGoogle(event.idToken)
            AuthUiEvent.SendPasswordResetEmail -> sendPasswordResetEmail()
            AuthUiEvent.SignOut -> signOut()
            AuthUiEvent.ErrorShown -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun signIn() = launchAuthAction {
        authUseCases.signInWithEmail(_uiState.value.email, _uiState.value.password)
    }

    private fun register() = launchAuthAction {
        val state = _uiState.value
        authUseCases.registerWithEmail(
            email = state.email,
            password = state.password,
            confirmPassword = state.confirmPassword,
            displayName = state.displayName,
        )
    }

    private fun continueAsGuest() = launchAuthAction {
        authUseCases.continueAsGuest()
    }

    private fun signInWithGoogle(idToken: String) = launchAuthAction {
        authUseCases.signInWithGoogle(idToken)
    }

    private fun sendPasswordResetEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authUseCases.sendPasswordReset(_uiState.value.email)) {
                is Resource.Success ->
                    _uiState.update { it.copy(isLoading = false, passwordResetEmailSent = true) }

                is Resource.Error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authUseCases.signOut()
            _uiState.value = AuthUiState(isSignedOut = true)
        }
    }

    private fun launchAuthAction(action: suspend () -> Resource<User>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = action()) {
                is Resource.Success ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }

                is Resource.Error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }

                is Resource.Loading -> Unit
            }
        }
    }
}
