package com.civiq.app.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.UserRole
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads a single user's profile and lets an Admin change their [UserRole]. */
@HiltViewModel
class AdminUserDetailViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle[Screen.ARG_USER_ID])

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = adminUseCases.getUserById(userId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, user = result.data, selectedRole = result.data.role)
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun saveRole() {
        val state = _uiState.value
        val role = state.selectedRole
        if (role == null || state.user == null || role == state.user.role || state.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = adminUseCases.updateUserRole(userId, role)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true, user = it.user?.copy(role = role))
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
