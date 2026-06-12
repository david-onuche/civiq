package com.civiq.app.presentation.challenges

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.ErrorState
import com.civiq.app.presentation.components.LoadingState
import com.civiq.app.presentation.theme.extendedColors

/**
 * The Challenges tab: the user's current daily streak and a detailed view of
 * today's AI-generated daily challenge, with a button to start it.
 */
@Composable
fun ChallengesScreen(
    onStartChallenge: (category: QuizCategory, difficulty: QuestionDifficulty, challengeId: String?) -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.nav_challenges)) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))

            uiState.challenge == null -> ErrorState(
                message = uiState.error?.asString() ?: stringResource(R.string.home_daily_challenge_error),
                modifier = Modifier.padding(paddingValues),
                onRetry = viewModel::loadChallenge,
            )

            else -> {
                val challenge = uiState.challenge!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { StreakCard(streakCount = uiState.streakCount) }
                    item {
                        ChallengeCard(
                            challenge = challenge,
                            isCompleted = uiState.progress?.isCompleted == true,
                            onStart = { onStartChallenge(challenge.category, challenge.difficulty, challenge.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(streakCount: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.streakContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.streak,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = streakCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.streak,
                    )
                    Text(
                        text = stringResource(R.string.home_streak_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.challenges_streak_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: DailyChallenge,
    isCompleted: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.home_daily_challenge_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                DifficultyBadge(difficulty = challenge.difficulty)
            }
            Text(
                text = "${challenge.category.emoji} ${challenge.title}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.home_daily_challenge_reward, challenge.xpReward, challenge.coinReward),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.extendedColors.xp,
                )
                Text(
                    text = stringResource(R.string.challenges_question_count, challenge.questionIds.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isCompleted) {
                CiviQOutlinedButton(
                    text = stringResource(R.string.home_daily_challenge_completed),
                    onClick = {},
                    enabled = false,
                )
            } else {
                CiviQButton(
                    text = stringResource(R.string.home_daily_challenge_start),
                    onClick = onStart,
                )
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: QuestionDifficulty, modifier: Modifier = Modifier) {
    val color = difficultyColor(difficulty)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = difficulty.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun difficultyColor(difficulty: QuestionDifficulty): Color = when (difficulty) {
    QuestionDifficulty.BEGINNER -> MaterialTheme.extendedColors.difficultyBeginner
    QuestionDifficulty.INTERMEDIATE -> MaterialTheme.extendedColors.difficultyIntermediate
    QuestionDifficulty.ADVANCED -> MaterialTheme.extendedColors.difficultyAdvanced
    QuestionDifficulty.EXPERT -> MaterialTheme.extendedColors.difficultyExpert
}
