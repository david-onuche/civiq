package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Lists the most recently created users and lets an Admin filter them by [UserRole]. */
@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    adminUseCases: AdminUseCases,
) : ViewModel() {

    private val usersResource: StateFlow<Resource<List<User>>> = adminUseCases.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    private val roleFilter = MutableStateFlow<UserRole?>(null)

    val uiState: StateFlow<AdminUsersUiState> = combine(usersResource, roleFilter) { users, filter ->
        AdminUsersUiState(users = users, roleFilter = filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUsersUiState())

    fun onRoleFilterChanged(role: UserRole?) {
        roleFilter.update { role }
    }
}
