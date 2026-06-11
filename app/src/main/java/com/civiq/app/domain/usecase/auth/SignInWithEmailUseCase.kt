package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.isValidEmail
import javax.inject.Inject

/** Validates input and signs the user in with an email/password credential. */
class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return Resource.Error(UiText.DynamicString("Email and password are required."))
        }
        if (!trimmedEmail.isValidEmail()) {
            return Resource.Error(UiText.DynamicString("Enter a valid email address."))
        }
        return authRepository.signInWithEmail(trimmedEmail, password)
    }
}
