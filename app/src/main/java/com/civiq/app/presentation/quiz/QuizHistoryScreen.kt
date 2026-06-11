package com.civiq.app.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.theme.extendedColors
import com.civiq.app.utils.toRelativeTimeString

/** Lists the current user's past quiz attempts, most recent first. */
@Composable
fun QuizHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: QuizHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = stringResource(R.string.quiz_history_title),
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        ResourceContent(
            resource = uiState,
            modifier = Modifier.padding(paddingValues),
        ) { attempts ->
            if (attempts.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.quiz_history_empty_title),
                    subtitle = stringResource(R.string.quiz_history_empty_subtitle),
                    icon = Icons.Filled.History,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(attempts, key = { it.id }) { attempt ->
                        QuizAttemptCard(attempt = attempt)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizAttemptCard(attempt: QuizAttempt, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = attempt.category.emoji, style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.quiz_history_item_subtitle,
                        attempt.score,
                        attempt.totalQuestions,
                        attempt.difficulty.displayName,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.quiz_result_xp_earned, attempt.xpEarned),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.extendedColors.xp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = attempt.completedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
