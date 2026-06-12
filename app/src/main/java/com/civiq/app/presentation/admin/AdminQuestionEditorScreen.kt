package com.civiq.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.CiviQTextField
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState

/**
 * Create/edit form for a single [com.civiq.app.domain.model.Question]. Saves
 * via [AdminQuestionEditorViewModel.save] and pops back on success.
 */
@Composable
fun AdminQuestionEditorScreen(
    onBackClick: () -> Unit,
    viewModel: AdminQuestionEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBackClick()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = if (uiState.isEditMode) {
                    stringResource(R.string.admin_question_editor_edit_title)
                } else {
                    stringResource(R.string.admin_question_editor_add_title)
                },
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AdminDropdownField(
                    label = stringResource(R.string.admin_category_label),
                    selectedLabel = "${uiState.category.emoji} ${uiState.category.displayName}",
                    options = QuizCategory.entries,
                    optionLabel = { "${it.emoji} ${it.displayName}" },
                    onOptionSelected = viewModel::onCategoryChanged,
                )
                AdminDropdownField(
                    label = stringResource(R.string.admin_difficulty_label),
                    selectedLabel = uiState.difficulty.displayName,
                    options = QuestionDifficulty.entries,
                    optionLabel = { it.displayName },
                    onOptionSelected = viewModel::onDifficultyChanged,
                )
                AdminDropdownField(
                    label = stringResource(R.string.admin_question_editor_type),
                    selectedLabel = uiState.type.label(),
                    options = QuestionType.entries,
                    optionLabel = { it.label() },
                    onOptionSelected = viewModel::onTypeChanged,
                )
                AdminDropdownField(
                    label = stringResource(R.string.admin_question_editor_tone),
                    selectedLabel = uiState.tone.label(),
                    options = QuestionTone.entries,
                    optionLabel = { it.label() },
                    onOptionSelected = viewModel::onToneChanged,
                )
                CiviQTextField(
                    value = uiState.questionText,
                    onValueChange = viewModel::onQuestionTextChanged,
                    label = stringResource(R.string.admin_question_editor_question_label),
                    singleLine = false,
                )

                Text(
                    text = stringResource(R.string.admin_question_editor_options_label),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (uiState.type == QuestionType.TRUE_FALSE) {
                    uiState.options.forEachIndexed { index, optionText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.correctAnswerIndex == index,
                                    onClick = { viewModel.onCorrectAnswerSelected(index) },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = uiState.correctAnswerIndex == index,
                                onClick = { viewModel.onCorrectAnswerSelected(index) },
                            )
                            Text(text = optionText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    uiState.options.forEachIndexed { index, optionText ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = uiState.correctAnswerIndex == index,
                                onClick = { viewModel.onCorrectAnswerSelected(index) },
                            )
                            CiviQTextField(
                                value = optionText,
                                onValueChange = { viewModel.onOptionTextChanged(index, it) },
                                label = stringResource(R.string.admin_question_editor_option_label, index + 1),
                                modifier = Modifier.weight(1f),
                            )
                            if (uiState.options.size > AdminQuestionEditorUiState.MIN_OPTIONS) {
                                IconButton(onClick = { viewModel.onRemoveOption(index) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
                                }
                            }
                        }
                    }
                    if (uiState.options.size < AdminQuestionEditorUiState.MAX_OPTIONS) {
                        CiviQOutlinedButton(
                            text = stringResource(R.string.admin_question_editor_add_option),
                            onClick = viewModel::onAddOption,
                        )
                    }
                }

                CiviQTextField(
                    value = uiState.explanation,
                    onValueChange = viewModel::onExplanationChanged,
                    label = stringResource(R.string.admin_question_editor_explanation_label),
                    singleLine = false,
                )

                CiviQButton(
                    text = stringResource(R.string.admin_question_editor_save),
                    onClick = viewModel::save,
                    enabled = uiState.isValid,
                    isLoading = uiState.isSaving,
                )
            }
        }
    }
}

private fun QuestionType.label(): String = when (this) {
    QuestionType.MULTIPLE_CHOICE -> "Multiple Choice"
    QuestionType.TRUE_FALSE -> "True / False"
    QuestionType.SCENARIO -> "Scenario"
}

private fun QuestionTone.label(): String = when (this) {
    QuestionTone.EDUCATIONAL -> "Educational"
    QuestionTone.FUNNY -> "Funny"
    QuestionTone.SATIRICAL -> "Satirical"
}
