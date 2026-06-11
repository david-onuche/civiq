package com.civiq.app.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQPasswordField
import com.civiq.app.presentation.components.CiviQTextField
import com.civiq.app.presentation.components.CiviQTopAppBar

/** Email/password registration with display name, used to create a [com.civiq.app.domain.model.UserRole.REGISTERED] account. */
@Composable
fun RegisterScreen(
    onAuthenticated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Scaffold(
        topBar = {
            CiviQTopAppBar(title = stringResource(R.string.action_sign_up), onBackClick = onNavigateBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            CiviQTextField(
                value = uiState.displayName,
                onValueChange = { viewModel.onEvent(AuthUiEvent.DisplayNameChanged(it)) },
                label = stringResource(R.string.label_display_name),
            )
            Spacer(modifier = Modifier.height(12.dp))
            CiviQTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
                label = stringResource(R.string.label_email),
                keyboardType = KeyboardType.Email,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CiviQPasswordField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(AuthUiEvent.PasswordChanged(it)) },
                label = stringResource(R.string.label_password),
            )
            Spacer(modifier = Modifier.height(12.dp))
            CiviQPasswordField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(AuthUiEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.label_confirm_password),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_password_requirements),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))
            CiviQButton(
                text = stringResource(R.string.action_sign_up),
                onClick = { viewModel.onEvent(AuthUiEvent.Register) },
                isLoading = uiState.isLoading,
            )

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
