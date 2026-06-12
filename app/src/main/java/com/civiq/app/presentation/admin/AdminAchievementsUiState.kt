package com.civiq.app.presentation.admin

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCategory
import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText

data class AdminAchievementsUiState(
    val achievements: Resource<List<Achievement>> = Resource.Loading(),
    val editorState: AdminAchievementEditorState? = null,
    val deletingAchievementId: String? = null,
    val errorMessage: UiText? = null,
)

/**
 * Backs the create/edit [Achievement] dialog in [AdminAchievementsScreen]. A
 * `null` value of this type (in [AdminAchievementsUiState.editorState]) means
 * the dialog is closed; [achievementId] being `null` distinguishes "add" from
 * "edit" mode.
 */
data class AdminAchievementEditorState(
    val achievementId: String? = null,
    val title: String = "",
    val description: String = "",
    val iconName: String = "EmojiEvents",
    val category: AchievementCategory = AchievementCategory.MILESTONE,
    val criteriaType: AchievementCriteriaType = AchievementCriteriaType.QUIZZES_COMPLETED,
    val criteriaValue: String = "",
    val xpReward: String = "",
    val coinReward: String = "",
    val isSaving: Boolean = false,
) {
    val isEditMode: Boolean get() = achievementId != null

    val isValid: Boolean
        get() = title.isNotBlank() &&
            description.isNotBlank() &&
            criteriaValue.toIntOrNull() != null &&
            xpReward.toLongOrNull() != null &&
            coinReward.toLongOrNull() != null
}
