package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Exchanges a Google ID token (from Credential Manager / One Tap) for a CiviQ session. */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String): Resource<User> =
        authRepository.signInWithGoogle(idToken)
}
