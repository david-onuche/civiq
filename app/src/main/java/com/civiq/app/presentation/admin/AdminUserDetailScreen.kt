package com.civiq.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.civiq.app.domain.model.UserRole
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.ErrorState
import com.civiq.app.presentation.components.LoadingState

/**
 * Shows a single user's profile and lets an Admin change their [UserRole] via
 * [AdminUserDetailViewModel.saveRole]. Reached from [AdminUsersScreen].
 */
@Composable
fun AdminUserDetailScreen(
    onBackClick: () -> Unit,
    viewModel: AdminUserDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.admin_user_detail_save_success))
            viewModel.dismissSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_user_detail_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val user = uiState.user
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            user == null -> ErrorState(
                message = stringResource(R.string.admin_user_detail_not_found),
                modifier = Modifier.padding(paddingValues),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName.ifBlank { stringResource(R.string.home_default_name) },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            AdminRoleChip(role = user.role)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AdminUserStatItem(label = stringResource(R.string.admin_user_detail_stat_level), value = user.level.toString())
                            AdminUserStatItem(label = stringResource(R.string.admin_user_detail_stat_xp), value = user.xp.toString())
                            AdminUserStatItem(label = stringResource(R.string.admin_user_detail_stat_coins), value = user.coins.toString())
                            AdminUserStatItem(label = stringResource(R.string.admin_user_detail_stat_streak), value = user.streakCount.toString())
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.admin_user_detail_change_role),
                    style = MaterialTheme.typography.titleMedium,
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        UserRole.entries.forEach { role ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.selectedRole == role,
                                        onClick = { viewModel.onRoleSelected(role) },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = uiState.selectedRole == role,
                                    onClick = { viewModel.onRoleSelected(role) },
                                )
                                Text(text = roleLabel(role), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                CiviQButton(
                    text = stringResource(R.string.admin_user_detail_save),
                    onClick = viewModel::saveRole,
                    enabled = uiState.hasUnsavedChanges,
                    isLoading = uiState.isSaving,
                )
            }
        }
    }
}

@Composable
private fun AdminUserStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
