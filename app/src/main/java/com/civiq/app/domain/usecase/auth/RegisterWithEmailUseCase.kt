package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.isValidEmail
import com.civiq.app.utils.isValidPassword
import javax.inject.Inject

/** Validates registration input and creates a new email/password account with [User.role] = REGISTERED. */
class RegisterWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
    ): Resource<User> {
        val trimmedEmail = email.trim()
        val trimmedName = displayName.trim()

        if (trimmedEmail.isBlank() || password.isBlank() || trimmedName.isBlank()) {
            return Resource.Error(UiText.DynamicString("All fields are required."))
        }
        if (!trimmedEmail.isValidEmail()) {
            return Resource.Error(UiText.DynamicString("Enter a valid email address."))
        }
        if (!password.isValidPassword()) {
            return Resource.Error(
                UiText.DynamicString("Password must be at least 8 characters and include a letter and a number."),
            )
        }
        if (password != confirmPassword) {
            return Resource.Error(UiText.DynamicString("Passwords do not match."))
        }

        return authRepository.registerWithEmail(trimmedEmail, password, trimmedName)
    }
}
