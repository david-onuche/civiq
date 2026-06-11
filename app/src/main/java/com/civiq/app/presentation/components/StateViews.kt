package com.civiq.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.civiq.app.R
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText

/** Centered, full-size loading spinner shown while data is being fetched. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Centered error state with icon, message, and an optional retry action. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ErrorOutline,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRetry != null) {
                CiviQOutlinedButton(text = stringResourceCompat(R.string.common_retry), onClick = onRetry)
            }
        }
    }
}

/** Offline-specific error state shown when [com.civiq.app.utils.NetworkConnectivityObserver] reports no connection. */
@Composable
fun OfflineState(modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    ErrorState(
        message = stringResourceCompat(R.string.common_error_network),
        modifier = modifier,
        icon = Icons.Filled.CloudOff,
        onRetry = onRetry,
    )
}

/** Centered empty state shown when a list/collection has no items yet. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Filled.Inbox,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Renders [resource] by delegating to [onLoading], [onError], or [onSuccess].
 * Centralizes the loading/error/empty/success branching used by nearly every
 * screen in the app.
 */
@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onLoading: @Composable () -> Unit = { LoadingState(modifier) },
    onError: @Composable (UiText) -> Unit = { ErrorState(it.asString(), modifier, onRetry = onRetry) },
    onSuccess: @Composable (T) -> Unit,
) {
    when (resource) {
        is Resource.Loading -> onLoading()
        is Resource.Error -> onError(resource.message)
        is Resource.Success -> onSuccess(resource.data)
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String =
    androidx.compose.ui.platform.LocalContext.current.getString(resId)
