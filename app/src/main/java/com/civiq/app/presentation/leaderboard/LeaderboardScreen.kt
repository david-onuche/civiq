package com.civiq.app.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.civiq.app.R
import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.domain.model.UserLevels
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.theme.extendedColors

/** The Leaderboard tab: ranked XP standings, filterable by time period and population scope. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.leaderboard_title)) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PeriodTabs(
                selected = uiState.period,
                onSelect = viewModel::selectPeriod,
            )
            ScopeSelector(
                selected = uiState.scope,
                onSelect = viewModel::selectScope,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
            ResourceContent(
                resource = uiState.entries,
                modifier = Modifier.fillMaxSize(),
            ) { entries ->
                if (entries.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.leaderboard_empty_title),
                        subtitle = stringResource(R.string.leaderboard_empty_subtitle),
                        icon = Icons.Filled.EmojiEvents,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    val showCurrentUserFooter = uiState.currentUserRank != null &&
                        entries.none { it.userId == uiState.currentUserId }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(entries, key = { it.userId }) { entry ->
                            LeaderboardEntryRow(
                                entry = entry,
                                isCurrentUser = entry.userId == uiState.currentUserId,
                            )
                        }
                        if (showCurrentUserFooter) {
                            item {
                                uiState.currentUserRank?.let { rank ->
                                    LeaderboardEntryRow(entry = rank, isCurrentUser = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodTabs(
    selected: LeaderboardPeriod,
    onSelect: (LeaderboardPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = LeaderboardPeriod.entries
    TabRow(selectedTabIndex = periods.indexOf(selected), modifier = modifier) {
        periods.forEach { period ->
            Tab(
                selected = period == selected,
                onClick = { onSelect(period) },
                text = { Text(period.displayName) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScopeSelector(
    selected: LeaderboardScope,
    onSelect: (LeaderboardScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scopes = LeaderboardScope.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        scopes.forEachIndexed { index, scope ->
            SegmentedButton(
                selected = scope == selected,
                onClick = { onSelect(scope) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = scopes.size),
                label = { Text(scope.displayName) },
            )
        }
    }
}

@Composable
private fun LeaderboardEntryRow(entry: LeaderboardEntry, isCurrentUser: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (isCurrentUser) {
                    it.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                } else {
                    it
                }
            },
        colors = if (isCurrentUser) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RankBadge(rank = entry.rank)
            if (entry.photoUrl != null) {
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser) {
                        "${entry.displayName} (${stringResource(R.string.leaderboard_you_label)})"
                    } else {
                        entry.displayName
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.home_level_label, entry.level, UserLevels.titleForLevel(entry.level)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.leaderboard_xp_value, entry.xp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.extendedColors.xp,
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int, modifier: Modifier = Modifier) {
    val isTopRank = rank == 1
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isTopRank) MaterialTheme.extendedColors.coinContainer else MaterialTheme.colorScheme.surfaceVariant,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.leaderboard_rank_format, rank),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isTopRank) MaterialTheme.extendedColors.coin else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
