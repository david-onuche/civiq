package com.civiq.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQTextField
import com.civiq.app.presentation.components.CiviQTopAppBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Schedules or replaces a single day's featured Daily Challenge. The question
 * set is auto-selected by [AdminChallengesViewModel.save] based on
 * [com.civiq.app.domain.model.QuizCategory] and
 * [com.civiq.app.domain.model.QuestionDifficulty] - no manual question picker
 * is needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChallengesScreen(
    onBackClick: () -> Unit,
    viewModel: AdminChallengesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.admin_challenges_save_success))
            viewModel.dismissSaveSuccess()
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
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_challenges_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.admin_challenges_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = uiState.date,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.admin_challenges_date_label)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.admin_challenges_date_label))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            CiviQTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = stringResource(R.string.admin_challenges_title_label),
            )
            CiviQTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = stringResource(R.string.admin_challenges_description_label),
                singleLine = false,
            )
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
            CiviQTextField(
                value = uiState.xpReward,
                onValueChange = viewModel::onXpRewardChanged,
                label = stringResource(R.string.admin_challenges_xp_reward_label),
                keyboardType = KeyboardType.Number,
            )
            CiviQTextField(
                value = uiState.coinReward,
                onValueChange = viewModel::onCoinRewardChanged,
                label = stringResource(R.string.admin_challenges_coin_reward_label),
                keyboardType = KeyboardType.Number,
            )

            CiviQButton(
                text = stringResource(R.string.admin_challenges_save),
                onClick = viewModel::save,
                enabled = uiState.isValid,
                isLoading = uiState.isSaving,
            )
        }
    }

    if (showDatePicker) {
        val initialMillis = LocalDate.parse(uiState.date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        viewModel.onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.common_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
