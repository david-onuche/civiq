package com.civiq.app.presentation.admin

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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent
import com.civiq.app.presentation.theme.extendedColors

/**
 * Lets an Admin remotely toggle [FeatureFlag]s (e.g. Premium perks) without a
 * release, backed by [AdminFeatureFlagsViewModel.onToggleFlag].
 */
@Composable
fun AdminFeatureFlagsScreen(
    onBackClick: () -> Unit,
    viewModel: AdminFeatureFlagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_feature_flags_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        ResourceContent(resource = uiState.flags, modifier = Modifier.padding(paddingValues)) { flags ->
            if (flags.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.admin_feature_flags_empty),
                    icon = Icons.Filled.Tune,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(flags, key = { it.key }) { flag ->
                        AdminFeatureFlagRow(
                            flag = flag,
                            isUpdating = flag.key in uiState.updatingKeys,
                            onToggle = { viewModel.onToggleFlag(flag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFeatureFlagRow(
    flag: FeatureFlag,
    isUpdating: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flag.description.ifBlank { flag.key },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(text = flag.key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (flag.requiresPremium) {
                    Text(
                        text = stringResource(R.string.profile_premium_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.coin,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Switch(checked = flag.isEnabled, onCheckedChange = { onToggle() }, enabled = !isUpdating)
        }
    }
}
