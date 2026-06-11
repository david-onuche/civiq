package com.civiq.app.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.civiq.app.presentation.components.CiviQTextField
import com.civiq.app.presentation.components.CiviQTopAppBar

/** Sends a Firebase Auth password reset email to the entered address. */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CiviQTopAppBar(title = stringResource(R.string.action_forgot_password_title), onBackClick = onNavigateBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.label_forgot_password_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            CiviQTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
                label = stringResource(R.string.label_email),
                keyboardType = KeyboardType.Email,
                enabled = !uiState.passwordResetEmailSent,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.passwordResetEmailSent) {
                Text(
                    text = stringResource(R.string.label_password_reset_sent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                CiviQButton(
                    text = stringResource(R.string.action_reset_password),
                    onClick = { viewModel.onEvent(AuthUiEvent.SendPasswordResetEmail) },
                    isLoading = uiState.isLoading,
                )
            }

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
