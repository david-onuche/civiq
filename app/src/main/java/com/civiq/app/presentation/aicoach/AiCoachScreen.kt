package com.civiq.app.presentation.aicoach

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.domain.model.CoachMessageRole
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState

/**
 * CiviQ's Premium "AI Learning Coach": a simple chat interface backed by
 * [AiCoachViewModel]. Users without Premium/Admin access (or when the
 * [com.civiq.app.domain.model.FeatureFlagKeys.AI_LEARNING_COACH] flag is
 * disabled) see an upsell prompting them to go Premium instead of the chat.
 */
@Composable
fun AiCoachScreen(
    onBackClick: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: AiCoachViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.ai_coach_title), onBackClick = onBackClick) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            !uiState.isAccessAllowed -> AiCoachLockedState(
                modifier = Modifier.padding(paddingValues),
                onUpgradeClick = onNavigateToPremium,
            )
            else -> AiCoachChat(
                uiState = uiState,
                onInputChanged = viewModel::onInputChanged,
                onSendClick = viewModel::sendMessage,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AiCoachLockedState(onUpgradeClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.ai_coach_locked_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.ai_coach_locked_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            CiviQButton(text = stringResource(R.string.ai_coach_upgrade_cta), onClick = onUpgradeClick)
        }
    }
}

@Composable
private fun AiCoachChat(
    uiState: AiCoachUiState,
    onInputChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true,
        ) {
            if (uiState.isSending) {
                item { CoachTypingIndicator() }
            }
            items(uiState.messages.reversed()) { message ->
                ChatBubble(message = message)
            }
        }
        uiState.errorMessage?.let { error ->
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        ChatInputRow(
            value = uiState.inputText,
            onValueChange = onInputChanged,
            onSendClick = onSendClick,
            isSending = uiState.isSending,
        )
    }
}

@Composable
private fun ChatBubble(message: CoachMessage, modifier: Modifier = Modifier) {
    val isUser = message.role == CoachMessageRole.USER
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CoachTypingIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun ChatInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ai_coach_input_placeholder)) },
            shape = MaterialTheme.shapes.large,
            maxLines = 4,
        )
        IconButton(onClick = onSendClick, enabled = value.isNotBlank() && !isSending) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.ai_coach_send))
        }
    }
}
