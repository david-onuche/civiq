package com.civiq.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.civiq.app.domain.model.AppNotification
import com.civiq.app.domain.model.NotificationType
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.theme.extendedColors
import com.civiq.app.utils.toRelativeTimeString

/**
 * The user's in-app notification feed: daily reminders, streak nudges,
 * achievement unlocks, weekly challenges, and admin broadcasts. Tapping an
 * unread notification marks it as read and follows its deep link, if any.
 */
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (route: String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = stringResource(R.string.notifications_title),
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = viewModel::markAllAsRead) {
                        Text(stringResource(R.string.notifications_mark_all_read))
                    }
                },
            )
        },
    ) { paddingValues ->
        ResourceContent(
            resource = uiState,
            modifier = Modifier.padding(paddingValues),
        ) { notifications ->
            if (notifications.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.notifications_empty_title),
                    subtitle = stringResource(R.string.notifications_empty_subtitle),
                    icon = Icons.Filled.Notifications,
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
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                viewModel.onNotificationClick(notification)
                                notification.deepLinkRoute?.let(onNotificationClick)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (icon, iconTint, iconContainer) = notificationStyle(notification.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = notification.createdAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun notificationStyle(type: NotificationType): Triple<ImageVector, Color, Color> = when (type) {
    NotificationType.DAILY_QUIZ -> Triple(
        Icons.Filled.MenuBook,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer,
    )
    NotificationType.STREAK_REMINDER -> Triple(
        Icons.Filled.LocalFireDepartment,
        MaterialTheme.extendedColors.streak,
        MaterialTheme.extendedColors.streakContainer,
    )
    NotificationType.ACHIEVEMENT_UNLOCKED -> Triple(
        Icons.Filled.EmojiEvents,
        MaterialTheme.extendedColors.coin,
        MaterialTheme.extendedColors.coinContainer,
    )
    NotificationType.WEEKLY_CHALLENGE -> Triple(
        Icons.Filled.AutoAwesome,
        MaterialTheme.extendedColors.xp,
        MaterialTheme.extendedColors.xpContainer,
    )
    NotificationType.SYSTEM -> Triple(
        Icons.Filled.Notifications,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant,
    )
}
