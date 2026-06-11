package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Starts a local-only [com.civiq.app.domain.model.UserRole.GUEST] session (no Firestore writes, progress not saved). */
class ContinueAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Resource<User> = authRepository.continueAsGuest()
}
