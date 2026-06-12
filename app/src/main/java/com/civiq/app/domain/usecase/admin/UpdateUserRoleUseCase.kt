package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.UserRole
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Promotes or demotes a user by changing their [UserRole], e.g. granting Admin or Premium access. */
class UpdateUserRoleUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(userId: String, role: UserRole): Resource<Unit> =
        adminRepository.updateUserRole(userId, role)
}
