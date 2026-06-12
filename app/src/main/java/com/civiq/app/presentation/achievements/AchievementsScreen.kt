package com.civiq.app.presentation.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.civiq.app.domain.model.AchievementWithStatus
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.components.achievementIcon
import com.civiq.app.presentation.theme.extendedColors
import com.civiq.app.utils.toRelativeTimeString

/**
 * Lists every achievement definition with the current user's unlock status:
 * unlocked badges show when they were earned and any rewards granted, locked
 * badges show progress toward their criteria.
 */
@Composable
fun AchievementsScreen(
    onBackClick: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = stringResource(R.string.achievements_title),
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        ResourceContent(
            resource = uiState,
            modifier = Modifier.padding(paddingValues),
        ) { achievements ->
            if (achievements.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.achievements_empty_title),
                    subtitle = stringResource(R.string.achievements_empty_subtitle),
                    icon = Icons.Filled.EmojiEvents,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            } else {
                val unlockedCount = achievements.count { it.isUnlocked }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = stringResource(
                                R.string.achievements_unlocked_count,
                                unlockedCount,
                                achievements.size,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(achievements, key = { it.achievement.id }) { item ->
                        AchievementCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(item: AchievementWithStatus, modifier: Modifier = Modifier) {
    val achievement = item.achievement
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isUnlocked) {
                            MaterialTheme.extendedColors.coinContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = achievementIcon(achievement.iconName),
                    contentDescription = null,
                    tint = if (item.isUnlocked) {
                        MaterialTheme.extendedColors.coin
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.isUnlocked) {
                    item.unlockedAt?.let { unlockedAt ->
                        Text(
                            text = stringResource(R.string.achievements_unlocked_on, unlockedAt.toRelativeTimeString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.extendedColors.coin,
                        )
                    }
                    if (achievement.xpReward > 0 || achievement.coinReward > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (achievement.xpReward > 0) {
                                RewardLabel(
                                    icon = Icons.Filled.Star,
                                    text = stringResource(R.string.quiz_result_xp_earned, achievement.xpReward),
                                    tint = MaterialTheme.extendedColors.xp,
                                )
                            }
                            if (achievement.coinReward > 0) {
                                RewardLabel(
                                    icon = Icons.Filled.MonetizationOn,
                                    text = stringResource(R.string.quiz_result_coins_earned, achievement.coinReward),
                                    tint = MaterialTheme.extendedColors.coin,
                                )
                            }
                        }
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { item.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.extendedColors.xp,
                        trackColor = MaterialTheme.extendedColors.xpContainer,
                    )
                    Text(
                        text = stringResource(
                            R.string.achievements_progress,
                            item.currentProgress,
                            achievement.criteriaValue,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardLabel(icon: ImageVector, text: String, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}
