package com.civiq.app.presentation.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState

/**
 * Landing screen of the Admin Dashboard, reachable from
 * [com.civiq.app.presentation.profile.ProfileScreen]'s menu for
 * [com.civiq.app.domain.model.User.isAdmin] users. Access is re-checked here
 * as a defense-in-depth measure: anyone else sees [AdminAccessDeniedState].
 */
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToQuestions: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToFeatureFlags: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_dashboard_title), onBackClick = onBackClick) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            !uiState.isAccessAllowed -> AdminAccessDeniedState(modifier = Modifier.padding(paddingValues))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.admin_dashboard_welcome, uiState.user?.displayName.orEmpty()),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                item {
                    AdminMenuItem(
                        icon = Icons.Filled.Group,
                        title = stringResource(R.string.admin_users_title),
                        subtitle = stringResource(R.string.admin_users_subtitle),
                        onClick = onNavigateToUsers,
                    )
                }
                item {
                    AdminMenuItem(
                        icon = Icons.Filled.MenuBook,
                        title = stringResource(R.string.admin_questions_title),
                        subtitle = stringResource(R.string.admin_questions_subtitle),
                        onClick = onNavigateToQuestions,
                    )
                }
                item {
                    AdminMenuItem(
                        icon = Icons.Filled.Bolt,
                        title = stringResource(R.string.admin_challenges_title),
                        subtitle = stringResource(R.string.admin_challenges_subtitle),
                        onClick = onNavigateToChallenges,
                    )
                }
                item {
                    AdminMenuItem(
                        icon = Icons.Filled.EmojiEvents,
                        title = stringResource(R.string.admin_achievements_title),
                        subtitle = stringResource(R.string.admin_achievements_subtitle),
                        onClick = onNavigateToAchievements,
                    )
                }
                item {
                    AdminMenuItem(
                        icon = Icons.Filled.Tune,
                        title = stringResource(R.string.admin_feature_flags_title),
                        subtitle = stringResource(R.string.admin_feature_flags_subtitle),
                        onClick = onNavigateToFeatureFlags,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminAccessDeniedState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(R.string.admin_access_denied_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.admin_access_denied_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
