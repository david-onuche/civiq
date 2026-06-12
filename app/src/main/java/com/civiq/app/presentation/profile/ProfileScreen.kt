package com.civiq.app.presentation.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.civiq.app.R
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserLevel
import com.civiq.app.domain.model.UserRole
import com.civiq.app.presentation.auth.AuthUiEvent
import com.civiq.app.presentation.auth.AuthViewModel
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState
import com.civiq.app.presentation.theme.extendedColors

/**
 * The Profile tab: the signed-in user's identity, level/XP progress, and
 * gamification stats, plus navigation to quiz history, achievements, account
 * settings, and sign out.
 */
@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
    onNavigateToQuizHistory: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToAiCoach: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authUiState.isSignedOut) {
        if (authUiState.isSignedOut) onSignedOut()
    }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.nav_profile)) },
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
            item { ProfileHeader(user = user) }
            item { ProfileLevelCard(userLevel = uiState.userLevel) }
            item { ProfileStatsRow(user = user, userLevel = uiState.userLevel) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Filled.WorkspacePremium,
                        title = if (user.isPremium) {
                            stringResource(R.string.profile_menu_manage_subscription)
                        } else {
                            stringResource(R.string.profile_menu_go_premium)
                        },
                        onClick = onNavigateToPremium,
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.AutoAwesome,
                        title = stringResource(R.string.profile_menu_ai_coach),
                        onClick = onNavigateToAiCoach,
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.History,
                        title = stringResource(R.string.quiz_history_title),
                        onClick = onNavigateToQuizHistory,
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.EmojiEvents,
                        title = stringResource(R.string.achievements_title),
                        onClick = onNavigateToAchievements,
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.Person,
                        title = stringResource(R.string.profile_menu_edit_profile),
                        onClick = onNavigateToEditProfile,
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.profile_menu_settings),
                        onClick = onNavigateToSettings,
                    )
                    if (user.isAdmin) {
                        ProfileMenuItem(
                            icon = Icons.Filled.AdminPanelSettings,
                            title = stringResource(R.string.admin_dashboard_title),
                            onClick = onNavigateToAdminDashboard,
                        )
                    }
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = stringResource(R.string.action_sign_out),
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { authViewModel.onEvent(AuthUiEvent.SignOut) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (user.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = user.displayName.ifBlank { stringResource(R.string.home_default_name) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (user.role == UserRole.PREMIUM || user.role == UserRole.ADMIN) {
                RoleBadge(role = user.role)
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (icon, label, tint, container) = when (role) {
        UserRole.PREMIUM -> RoleBadgeStyle(
            Icons.Filled.WorkspacePremium,
            stringResource(R.string.profile_premium_badge),
            MaterialTheme.extendedColors.coin,
            MaterialTheme.extendedColors.coinContainer,
        )
        UserRole.ADMIN -> RoleBadgeStyle(
            Icons.Filled.AdminPanelSettings,
            stringResource(R.string.profile_admin_badge),
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
        else -> return
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}

private data class RoleBadgeStyle(val icon: ImageVector, val label: String, val tint: Color, val container: Color)

@Composable
private fun ProfileLevelCard(userLevel: UserLevel, modifier: Modifier = Modifier) {
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
private fun ProfileStatsRow(user: User, userLevel: UserLevel, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileStatChip(
            icon = Icons.Filled.Star,
            value = userLevel.currentXp.toString(),
            label = stringResource(R.string.profile_stat_total_xp),
            color = MaterialTheme.extendedColors.xp,
            containerColor = MaterialTheme.extendedColors.xpContainer,
            modifier = Modifier.weight(1f),
        )
        ProfileStatChip(
            icon = Icons.Filled.MonetizationOn,
            value = user.coins.toString(),
            label = stringResource(R.string.home_coins_label),
            color = MaterialTheme.extendedColors.coin,
            containerColor = MaterialTheme.extendedColors.coinContainer,
            modifier = Modifier.weight(1f),
        )
        ProfileStatChip(
            icon = Icons.Filled.LocalFireDepartment,
            value = user.streakCount.toString(),
            label = stringResource(R.string.home_streak_label),
            color = MaterialTheme.extendedColors.streak,
            containerColor = MaterialTheme.extendedColors.streakContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileStatChip(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
