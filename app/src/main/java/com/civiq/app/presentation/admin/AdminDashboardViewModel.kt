package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.usecase.admin.AdminUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives the Admin Dashboard's landing screen: confirms the signed-in user
 * holds [com.civiq.app.domain.model.UserRole.ADMIN] before exposing
 * navigation to the user, question, daily challenge, achievement, and
 * feature flag management screens.
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    adminUseCases: AdminUseCases,
) : ViewModel() {

    val uiState: StateFlow<AdminDashboardUiState> = adminUseCases.observeCurrentUser()
        .map { user -> AdminDashboardUiState(isLoading = false, isAccessAllowed = user?.isAdmin == true, user = user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminDashboardUiState())
}
