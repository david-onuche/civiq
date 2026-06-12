package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.usecase.admin.AdminUseCases
import com.civiq.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Schedules or replaces a single day's [DailyChallenge] via
 * [AdminUseCases.createOrUpdateDailyChallenge], which auto-selects the
 * challenge's question set from the chosen category and difficulty.
 */
@HiltViewModel
class AdminChallengesViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
) : ViewModel() {

    private val defaults = DailyChallenge()

    private val _uiState = MutableStateFlow(
        AdminChallengesUiState(
            date = LocalDate.now().toString(),
            category = defaults.category,
            difficulty = defaults.difficulty,
            xpReward = defaults.xpReward.toString(),
            coinReward = defaults.coinReward.toString(),
        ),
    )
    val uiState: StateFlow<AdminChallengesUiState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date.toString()) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCategoryChanged(category: QuizCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDifficultyChanged(difficulty: QuestionDifficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun onXpRewardChanged(value: String) {
        _uiState.update { it.copy(xpReward = value) }
    }

    fun onCoinRewardChanged(value: String) {
        _uiState.update { it.copy(coinReward = value) }
    }

    fun save() {
        val state = _uiState.value
        val xp = state.xpReward.toLongOrNull()
        val coins = state.coinReward.toLongOrNull()
        if (!state.isValid || xp == null || coins == null || state.isSaving) return

        val challenge = DailyChallenge(
            id = state.date,
            date = state.date,
            title = state.title.trim(),
            description = state.description.trim(),
            category = state.category,
            difficulty = state.difficulty,
            xpReward = xp,
            coinReward = coins,
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = adminUseCases.createOrUpdateDailyChallenge(challenge)) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
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
