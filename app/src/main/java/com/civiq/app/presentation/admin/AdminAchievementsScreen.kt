package com.civiq.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCategory
import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.presentation.components.CiviQTextField
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.components.achievementIcon
import com.civiq.app.presentation.theme.extendedColors

/**
 * Lists every [Achievement] definition, with an inline AlertDialog editor
 * (driven by [AdminAchievementEditorState]) for create/edit and a
 * confirmation dialog for delete.
 */
@Composable
fun AdminAchievementsScreen(
    onBackClick: () -> Unit,
    viewModel: AdminAchievementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var achievementPendingDelete by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_achievements_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.admin_achievements_add))
            }
        },
    ) { paddingValues ->
        ResourceContent(resource = uiState.achievements, modifier = Modifier.fillMaxSize().padding(paddingValues)) { achievements ->
            if (achievements.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.admin_achievements_empty_title),
                    icon = Icons.Filled.EmojiEvents,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(achievements, key = { it.id }) { achievement ->
                        AdminAchievementRow(
                            achievement = achievement,
                            isDeleting = uiState.deletingAchievementId == achievement.id,
                            onClick = { viewModel.onEditClick(achievement) },
                            onDeleteClick = { achievementPendingDelete = achievement },
                        )
                    }
                }
            }
        }
    }

    val achievementToDelete = achievementPendingDelete
    if (achievementToDelete != null) {
        AlertDialog(
            onDismissRequest = { achievementPendingDelete = null },
            title = { Text(stringResource(R.string.admin_achievements_delete_title)) },
            text = { Text(stringResource(R.string.admin_achievements_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteAchievement(achievementToDelete.id)
                    achievementPendingDelete = null
                }) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { achievementPendingDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    val editor = uiState.editorState
    if (editor != null) {
        AlertDialog(
            onDismissRequest = viewModel::onEditorDismiss,
            title = {
                Text(
                    if (editor.isEditMode) {
                        stringResource(R.string.admin_achievements_editor_edit_title)
                    } else {
                        stringResource(R.string.admin_achievements_editor_add_title)
                    },
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CiviQTextField(
                        value = editor.title,
                        onValueChange = viewModel::onEditorTitleChanged,
                        label = stringResource(R.string.admin_achievements_editor_title_label),
                    )
                    CiviQTextField(
                        value = editor.description,
                        onValueChange = viewModel::onEditorDescriptionChanged,
                        label = stringResource(R.string.admin_achievements_editor_description_label),
                        singleLine = false,
                    )
                    AdminDropdownField(
                        label = stringResource(R.string.admin_achievements_editor_icon_label),
                        selectedLabel = iconDisplayName(editor.iconName),
                        options = ACHIEVEMENT_ICON_NAMES,
                        optionLabel = { iconDisplayName(it) },
                        onOptionSelected = viewModel::onEditorIconChanged,
                    )
                    AdminDropdownField(
                        label = stringResource(R.string.admin_category_label),
                        selectedLabel = editor.category.label(),
                        options = AchievementCategory.entries,
                        optionLabel = { it.label() },
                        onOptionSelected = viewModel::onEditorCategoryChanged,
                    )
                    AdminDropdownField(
                        label = stringResource(R.string.admin_achievements_editor_criteria_type_label),
                        selectedLabel = editor.criteriaType.label(),
                        options = AchievementCriteriaType.entries,
                        optionLabel = { it.label() },
                        onOptionSelected = viewModel::onEditorCriteriaTypeChanged,
                    )
                    CiviQTextField(
                        value = editor.criteriaValue,
                        onValueChange = viewModel::onEditorCriteriaValueChanged,
                        label = stringResource(R.string.admin_achievements_editor_criteria_value_label),
                        keyboardType = KeyboardType.Number,
                    )
                    CiviQTextField(
                        value = editor.xpReward,
                        onValueChange = viewModel::onEditorXpRewardChanged,
                        label = stringResource(R.string.admin_achievements_editor_xp_reward_label),
                        keyboardType = KeyboardType.Number,
                    )
                    CiviQTextField(
                        value = editor.coinReward,
                        onValueChange = viewModel::onEditorCoinRewardChanged,
                        label = stringResource(R.string.admin_achievements_editor_coin_reward_label),
                        keyboardType = KeyboardType.Number,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onEditorSave,
                    enabled = editor.isValid && !editor.isSaving,
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onEditorDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun AdminAchievementRow(
    achievement: Achievement,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.coinContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = achievementIcon(achievement.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.coin,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${achievement.category.label()} • ${achievement.criteriaType.label()}: " +
                        "${achievement.criteriaValue} • +${achievement.xpReward} XP, +${achievement.coinReward} coins",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick, enabled = !isDeleting) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val ACHIEVEMENT_ICON_NAMES = listOf(
    "EmojiEvents",
    "Star",
    "LocalFireDepartment",
    "School",
    "WorkspacePremium",
    "Bolt",
    "Public",
    "MenuBook",
    "Diversity3",
    "Verified",
    "Gavel",
    "HowToVote",
    "AutoAwesome",
)

private fun iconDisplayName(iconName: String): String = when (iconName) {
    "EmojiEvents" -> "Trophy"
    "Star" -> "Star"
    "LocalFireDepartment" -> "Fire"
    "School" -> "Graduation Cap"
    "WorkspacePremium" -> "Premium Medal"
    "Bolt" -> "Lightning Bolt"
    "Public" -> "Globe"
    "MenuBook" -> "Open Book"
    "Diversity3" -> "Community"
    "Verified" -> "Verified Check"
    "Gavel" -> "Gavel"
    "HowToVote" -> "Ballot Box"
    "AutoAwesome" -> "Sparkle"
    else -> iconName
}

private fun AchievementCategory.label(): String = when (this) {
    AchievementCategory.MILESTONE -> "Milestone"
    AchievementCategory.STREAK -> "Streak"
    AchievementCategory.CATEGORY_MASTERY -> "Category Mastery"
    AchievementCategory.SOCIAL -> "Social"
    AchievementCategory.SPECIAL -> "Special"
}

private fun AchievementCriteriaType.label(): String = when (this) {
    AchievementCriteriaType.QUIZZES_COMPLETED -> "Quizzes Completed"
    AchievementCriteriaType.PERFECT_SCORES -> "Perfect Scores"
    AchievementCriteriaType.STREAK_DAYS -> "Streak Days"
    AchievementCriteriaType.XP_EARNED -> "XP Earned"
    AchievementCriteriaType.CATEGORY_QUESTIONS_ANSWERED -> "Category Questions"
    AchievementCriteriaType.DAILY_CHALLENGES_COMPLETED -> "Daily Challenges"
}
