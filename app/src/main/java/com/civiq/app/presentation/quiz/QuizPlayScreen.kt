package com.civiq.app.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.Question
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.ErrorState
import com.civiq.app.presentation.components.LoadingState
import com.civiq.app.presentation.theme.extendedColors

/** A single quiz session: one question at a time, with immediate answer feedback. */
@Composable
fun QuizPlayScreen(
    onNavigateToResult: (attemptId: String) -> Unit,
    onExit: () -> Unit,
    viewModel: QuizPlayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuizPlayNavigationEvent.NavigateToResult -> onNavigateToResult(event.attemptId)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null && uiState.questions.isNotEmpty()) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.onEvent(QuizPlayUiEvent.ErrorShown)
        }
    }

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = if (uiState.questions.isNotEmpty()) {
                    stringResource(R.string.quiz_question_progress, uiState.currentIndex + 1, uiState.questions.size)
                } else {
                    "${uiState.category.emoji} ${uiState.category.displayName}"
                },
                onBackClick = onExit,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            uiState.questions.isEmpty() -> ErrorState(
                message = uiState.errorMessage?.asString() ?: stringResource(R.string.common_error_generic),
                modifier = Modifier.padding(paddingValues),
                onRetry = onExit,
            )
            else -> QuizContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun QuizContent(
    uiState: QuizPlayUiState,
    onEvent: (QuizPlayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                QuestionOptionCard(
                    text = option,
                    state = optionState(uiState, index, question),
                    enabled = !uiState.isAnswerRevealed,
                    onClick = { onEvent(QuizPlayUiEvent.SelectAnswer(index)) },
                )
            }
        }
        if (uiState.isAnswerRevealed && question.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.quiz_explanation_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = question.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        CiviQButton(
            text = if (uiState.isLastQuestion) {
                stringResource(R.string.quiz_action_finish)
            } else {
                stringResource(R.string.quiz_action_next)
            },
            onClick = { onEvent(QuizPlayUiEvent.NextQuestion) },
            enabled = uiState.isAnswerRevealed,
            isLoading = uiState.isSubmitting && uiState.isLastQuestion,
        )
    }
}

private enum class OptionState { DEFAULT, CORRECT, INCORRECT }

private fun optionState(uiState: QuizPlayUiState, index: Int, question: Question): OptionState = when {
    !uiState.isAnswerRevealed -> OptionState.DEFAULT
    index == question.correctAnswerIndex -> OptionState.CORRECT
    index == uiState.selectedAnswerIndex -> OptionState.INCORRECT
    else -> OptionState.DEFAULT
}

@Composable
private fun QuestionOptionCard(
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor: Color
    val contentColor: Color
    val icon = when (state) {
        OptionState.CORRECT -> {
            containerColor = MaterialTheme.extendedColors.correctContainer
            contentColor = MaterialTheme.extendedColors.correct
            Icons.Filled.CheckCircle
        }
        OptionState.INCORRECT -> {
            containerColor = MaterialTheme.extendedColors.incorrectContainer
            contentColor = MaterialTheme.extendedColors.incorrect
            Icons.Filled.Cancel
        }
        OptionState.DEFAULT -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            null
        }
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            }
        }
    }
}
