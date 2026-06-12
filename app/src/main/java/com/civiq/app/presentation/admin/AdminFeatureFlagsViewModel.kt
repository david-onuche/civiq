package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Lets an Admin toggle remotely-configured [FeatureFlag]s on or off. */
@HiltViewModel
class AdminFeatureFlagsViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
) : ViewModel() {

    private val flagsResource: StateFlow<Resource<List<FeatureFlag>>> = adminUseCases.observeFeatureFlags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    private val updatingKeys = MutableStateFlow<Set<String>>(emptySet())
    private val errorMessage = MutableStateFlow<UiText?>(null)

    val uiState: StateFlow<AdminFeatureFlagsUiState> = combine(
        flagsResource,
        updatingKeys,
        errorMessage,
    ) { flags, updating, error ->
        AdminFeatureFlagsUiState(flags = flags, updatingKeys = updating, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminFeatureFlagsUiState())

    fun onToggleFlag(flag: FeatureFlag) {
        if (flag.key in updatingKeys.value) return
        updatingKeys.update { it + flag.key }
        viewModelScope.launch {
            val result = adminUseCases.updateFeatureFlag(flag.copy(isEnabled = !flag.isEnabled))
            if (result is Resource.Error) {
                errorMessage.update { result.message }
            }
            updatingKeys.update { it - flag.key }
        }
    }

    fun dismissError() {
        errorMessage.update { null }
    }
}
