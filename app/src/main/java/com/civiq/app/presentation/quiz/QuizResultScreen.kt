package com.civiq.app.presentation.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.theme.extendedColors
import com.civiq.app.utils.toPercentString

/**
 * Shows the outcome of a finished quiz: score, accuracy, XP/coin rewards,
 * and a perfect-score badge, with actions to return home or try again.
 */
@Composable
fun QuizResultScreen(
    onDone: () -> Unit,
    onRetry: (category: QuizCategory, difficulty: QuestionDifficulty) -> Unit,
    viewModel: QuizResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        ResourceContent(
            resource = uiState,
            modifier = Modifier.padding(paddingValues),
            onRetry = viewModel::loadAttempt,
        ) { attempt ->
            ResultContent(
                attempt = attempt,
                onDone = onDone,
                onRetry = { onRetry(attempt.category, attempt.difficulty) },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ResultContent(
    attempt: QuizAttempt,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    if (attempt.isPerfectScore) {
                        MaterialTheme.extendedColors.coinContainer
                    } else {
                        MaterialTheme.extendedColors.xpContainer
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (attempt.isPerfectScore) Icons.Filled.EmojiEvents else Icons.Filled.Star,
                contentDescription = null,
                tint = if (attempt.isPerfectScore) MaterialTheme.extendedColors.coin else MaterialTheme.extendedColors.xp,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.quiz_result_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (attempt.isPerfectScore) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.quiz_result_perfect),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.extendedColors.coin,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.quiz_result_score, attempt.score, attempt.totalQuestions),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = attempt.accuracy.toDouble().toPercentString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            RewardChip(
                icon = Icons.Filled.Star,
                text = stringResource(R.string.quiz_result_xp_earned, attempt.xpEarned),
                tint = MaterialTheme.extendedColors.xp,
            )
            RewardChip(
                icon = Icons.Filled.MonetizationOn,
                text = stringResource(R.string.quiz_result_coins_earned, attempt.coinsEarned),
                tint = MaterialTheme.extendedColors.coin,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        CiviQButton(text = stringResource(R.string.quiz_result_done), onClick = onDone)
        Spacer(modifier = Modifier.height(8.dp))
        CiviQOutlinedButton(text = stringResource(R.string.quiz_result_retry), onClick = onRetry)
    }
}

@Composable
private fun RewardChip(icon: ImageVector, text: String, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
