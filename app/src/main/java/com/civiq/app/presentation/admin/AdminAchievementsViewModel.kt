package com.civiq.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCategory
import com.civiq.app.domain.model.AchievementCriteriaType
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

/**
 * Backs [AdminAchievementsScreen]: lists all [Achievement] definitions and
 * drives the inline create/edit dialog ([AdminAchievementEditorState]) and
 * per-row delete actions.
 */
@HiltViewModel
class AdminAchievementsViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,
) : ViewModel() {

    private val achievementsResource = adminUseCases.observeAchievementDefinitions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading())

    private val editorState = MutableStateFlow<AdminAchievementEditorState?>(null)
    private val deletingAchievementId = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<UiText?>(null)

    val uiState: StateFlow<AdminAchievementsUiState> = combine(
        achievementsResource,
        editorState,
        deletingAchievementId,
        errorMessage,
    ) { achievements, editor, deletingId, error ->
        AdminAchievementsUiState(
            achievements = achievements,
            editorState = editor,
            deletingAchievementId = deletingId,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminAchievementsUiState())

    fun onAddClick() {
        editorState.value = AdminAchievementEditorState()
    }

    fun onEditClick(achievement: Achievement) {
        editorState.value = AdminAchievementEditorState(
            achievementId = achievement.id,
            title = achievement.title,
            description = achievement.description,
            iconName = achievement.iconName,
            category = achievement.category,
            criteriaType = achievement.criteriaType,
            criteriaValue = achievement.criteriaValue.toString(),
            xpReward = achievement.xpReward.toString(),
            coinReward = achievement.coinReward.toString(),
        )
    }

    fun onEditorDismiss() {
        editorState.value = null
    }

    fun onEditorTitleChanged(title: String) {
        editorState.update { it?.copy(title = title) }
    }

    fun onEditorDescriptionChanged(description: String) {
        editorState.update { it?.copy(description = description) }
    }

    fun onEditorIconChanged(iconName: String) {
        editorState.update { it?.copy(iconName = iconName) }
    }

    fun onEditorCategoryChanged(category: AchievementCategory) {
        editorState.update { it?.copy(category = category) }
    }

    fun onEditorCriteriaTypeChanged(criteriaType: AchievementCriteriaType) {
        editorState.update { it?.copy(criteriaType = criteriaType) }
    }

    fun onEditorCriteriaValueChanged(value: String) {
        editorState.update { it?.copy(criteriaValue = value) }
    }

    fun onEditorXpRewardChanged(value: String) {
        editorState.update { it?.copy(xpReward = value) }
    }

    fun onEditorCoinRewardChanged(value: String) {
        editorState.update { it?.copy(coinReward = value) }
    }

    fun onEditorSave() {
        val editor = editorState.value ?: return
        val criteriaValue = editor.criteriaValue.toIntOrNull()
        val xp = editor.xpReward.toLongOrNull()
        val coins = editor.coinReward.toLongOrNull()
        if (!editor.isValid || criteriaValue == null || xp == null || coins == null || editor.isSaving) return

        val achievement = Achievement(
            id = editor.achievementId ?: "",
            title = editor.title.trim(),
            description = editor.description.trim(),
            iconName = editor.iconName,
            category = editor.category,
            criteriaType = editor.criteriaType,
            criteriaValue = criteriaValue,
            xpReward = xp,
            coinReward = coins,
        )

        editorState.update { it?.copy(isSaving = true) }
        viewModelScope.launch {
            val result: Resource<*> = if (editor.isEditMode) {
                adminUseCases.updateAchievement(achievement)
            } else {
                adminUseCases.createAchievement(achievement)
            }
            when (result) {
                is Resource.Success -> editorState.value = null
                is Resource.Error -> {
                    errorMessage.value = result.message
                    editorState.update { it?.copy(isSaving = false) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteAchievement(achievementId: String) {
        deletingAchievementId.value = achievementId
        viewModelScope.launch {
            when (val result = adminUseCases.deleteAchievement(achievementId)) {
                is Resource.Success -> Unit
                is Resource.Error -> errorMessage.value = result.message
                is Resource.Loading -> Unit
            }
            deletingAchievementId.value = null
        }
    }

    fun dismissError() {
        errorMessage.value = null
    }
}
