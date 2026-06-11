package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the current session's [User], emitting `null` when signed out. Drives the Splash screen routing decision. */
class ObserveCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = authRepository.currentUser
}
