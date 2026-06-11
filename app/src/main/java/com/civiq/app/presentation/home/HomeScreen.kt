package com.civiq.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.civiq.app.R
import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserLevel
import com.civiq.app.presentation.auth.AuthUiEvent
import com.civiq.app.presentation.auth.AuthViewModel
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState
import com.civiq.app.presentation.components.achievementIcon
import com.civiq.app.presentation.theme.extendedColors
import java.util.Calendar

/**
 * The Home tab: greets the user, shows their level/XP progress, coin and
 * streak counts, today's daily challenge, a "continue learning" suggestion,
 * and recently unlocked achievements.
 */
@Composable
fun HomeScreen(
    onStartQuiz: (category: QuizCategory, difficulty: QuestionDifficulty, challengeId: String?) -> Unit,
    onNavigateToQuizHub: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authUiState.isSignedOut) {
        if (authUiState.isSignedOut) onSignedOut()
    }

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { authViewModel.onEvent(AuthUiEvent.SignOut) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.action_sign_out),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val user = uiState.user
        if (uiState.isLoading || user == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HomeHeader(user = user) }
            item { LevelProgressCard(userLevel = uiState.userLevel) }
            item { StatsRow(coins = user.coins, streakCount = user.streakCount) }
            item {
                DailyChallengeCard(
                    challenge = uiState.dailyChallenge,
                    isLoading = uiState.isDailyChallengeLoading,
                    progress = uiState.dailyChallengeProgress,
                    onStart = { challenge ->
                        onStartQuiz(challenge.category, challenge.difficulty, challenge.id)
                    },
                )
            }
            item {
                ContinueLearningCard(
                    category = uiState.continueCategory,
                    difficulty = uiState.continueDifficulty,
                    onContinue = { category, difficulty -> onStartQuiz(category, difficulty, null) },
                    onBrowse = onNavigateToQuizHub,
                )
            }
            item { RecentAchievementsSection(achievements = uiState.recentAchievements) }
        }
    }
}

@Composable
private fun HomeHeader(user: User, modifier: Modifier = Modifier) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingRes = when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
    val name = user.displayName.ifBlank { stringResource(R.string.home_default_name) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(greetingRes, name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (user.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LevelProgressCard(userLevel: UserLevel, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.extendedColors.xpContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = userLevel.level.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.extendedColors.xp,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_level_label, userLevel.level, userLevel.title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (userLevel.progress >= 1f) {
                            stringResource(R.string.home_max_level)
                        } else {
                            stringResource(
                                R.string.home_xp_to_next_level,
                                userLevel.xpRemainingForNextLevel,
                                userLevel.level + 1,
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { userLevel.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.extendedColors.xp,
                trackColor = MaterialTheme.extendedColors.xpContainer,
            )
        }
    }
}

@Composable
private fun StatsRow(coins: Long, streakCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatChip(
            icon = Icons.Filled.MonetizationOn,
            value = coins.toString(),
            label = stringResource(R.string.home_coins_label),
            color = MaterialTheme.extendedColors.coin,
            containerColor = MaterialTheme.extendedColors.coinContainer,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            icon = Icons.Filled.LocalFireDepartment,
            value = streakCount.toString(),
            label = stringResource(R.string.home_streak_label),
            color = MaterialTheme.extendedColors.streak,
            containerColor = MaterialTheme.extendedColors.streakContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Column {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    challenge: DailyChallenge?,
    isLoading: Boolean,
    progress: DailyChallengeProgress?,
    onStart: (DailyChallenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                )
            }
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }

                challenge == null -> {
                    Text(
                        text = stringResource(R.string.home_daily_challenge_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Text(
                        text = "${challenge.category.emoji} ${challenge.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.home_daily_challenge_reward, challenge.xpReward, challenge.coinReward),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.extendedColors.xp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (progress?.isCompleted == true) {
                        CiviQOutlinedButton(
                            text = stringResource(R.string.home_daily_challenge_completed),
                            onClick = {},
                            enabled = false,
                        )
                    } else {
                        CiviQButton(
                            text = stringResource(R.string.home_daily_challenge_start),
                            onClick = { onStart(challenge) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueLearningCard(
    category: QuizCategory?,
    difficulty: QuestionDifficulty?,
    onContinue: (QuizCategory, QuestionDifficulty) -> Unit,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (category != null && difficulty != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.home_continue_learning_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "${category.emoji} ${category.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.home_continue_learning_subtitle, difficulty.displayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                CiviQButton(
                    text = stringResource(R.string.common_continue),
                    onClick = { onContinue(category, difficulty) },
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.home_start_learning_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(R.string.home_start_learning_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                CiviQButton(
                    text = stringResource(R.string.home_browse_categories),
                    onClick = onBrowse,
                )
            }
        }
    }
}

@Composable
private fun RecentAchievementsSection(achievements: List<AchievementWithStatus>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.extendedColors.coin,
            )
            Text(
                text = stringResource(R.string.home_recent_achievements),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (achievements.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_no_achievements_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.home_no_achievements_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(achievements, key = { it.achievement.id }) { item ->
                    AchievementBadge(item = item)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(item: AchievementWithStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(88.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.extendedColors.coinContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = achievementIcon(item.achievement.iconName),
                contentDescription = null,
                tint = MaterialTheme.extendedColors.coin,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = item.achievement.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
