package com.civiq.app.presentation.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.R
import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.domain.model.CoachMessageRole
import com.civiq.app.domain.model.FeatureFlagKeys
import com.civiq.app.domain.usecase.aicoach.AiCoachUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Premium "AI Learning Coach" chat. Access requires the
 * signed-in user to be [com.civiq.app.domain.model.User.isPremium] or
 * [com.civiq.app.domain.model.User.isAdmin] AND the
 * [FeatureFlagKeys.AI_LEARNING_COACH] remote feature flag to be enabled.
 */
@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val aiCoachUseCases: AiCoachUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = aiCoachUseCases.observeCurrentUser().filterNotNull().first()
            val flagEnabled = aiCoachUseCases.isFeatureEnabled(FeatureFlagKeys.AI_LEARNING_COACH)
            val isAllowed = (user.isPremium || user.isAdmin) && flagEnabled
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAccessAllowed = isAllowed,
                    messages = if (isAllowed) {
                        listOf(CoachMessage(role = CoachMessageRole.COACH, content = WELCOME_MESSAGE))
                    } else {
                        emptyList()
                    },
                )
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        if (text.isBlank() || state.isSending) return

        val updatedHistory = state.messages + CoachMessage(role = CoachMessageRole.USER, content = text)
        _uiState.update { it.copy(messages = updatedHistory, inputText = "", isSending = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = aiCoachUseCases.sendCoachMessage(updatedHistory)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        messages = it.messages + CoachMessage(role = CoachMessageRole.COACH, content = result.data),
                        isSending = false,
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(isSending = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private companion object {
        const val WELCOME_MESSAGE = "Hi! I'm your CiviQ Learning Coach. Ask me anything about " +
            "democracy, government, civic rights, or how your community works - I'm here to help."
    }
}
