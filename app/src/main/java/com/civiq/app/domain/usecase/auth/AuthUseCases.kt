package com.civiq.app.domain.usecase.auth

import javax.inject.Inject

/**
 * Bundles all authentication use cases for single-point injection into
 * [com.civiq.app.presentation.auth.AuthViewModel] and friends.
 */
data class AuthUseCases @Inject constructor(
    val signInWithEmail: SignInWithEmailUseCase,
    val registerWithEmail: RegisterWithEmailUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val sendPasswordReset: SendPasswordResetUseCase,
    val signOut: SignOutUseCase,
    val continueAsGuest: ContinueAsGuestUseCase,
    val observeCurrentUser: ObserveCurrentUserUseCase,
)
