package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.isValidEmail
import javax.inject.Inject

/** Sends a "forgot password" email after validating the address format. */
class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.isValidEmail()) {
            return Resource.Error(UiText.DynamicString("Enter a valid email address."))
        }
        return authRepository.sendPasswordResetEmail(trimmedEmail)
    }
}
