package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.UserRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Loads a single user's profile for the Admin User Detail screen. */
class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: String): Resource<User> = userRepository.getUser(userId)
}
