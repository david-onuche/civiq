package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the most recently created users for the Admin Dashboard's user management screen. */
class ObserveAllUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    operator fun invoke(limit: Int = 50): Flow<Resource<List<User>>> = adminRepository.observeAllUsers(limit)
}
