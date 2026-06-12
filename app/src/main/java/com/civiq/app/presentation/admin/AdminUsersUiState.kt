package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.utils.Resource

data class AdminUsersUiState(
    val users: Resource<List<User>> = Resource.Loading(),
    val roleFilter: UserRole? = null,
) {
    /** [users], optionally narrowed to [roleFilter]. */
    val filteredUsers: Resource<List<User>>
        get() = when (users) {
            is Resource.Success -> Resource.Success(
                if (roleFilter == null) users.data else users.data.filter { it.role == roleFilter },
            )
            else -> users
        }
}
