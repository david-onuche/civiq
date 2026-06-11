package com.civiq.app.presentation.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.BuildConfig
import com.civiq.app.R
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.CiviQPasswordField
import com.civiq.app.presentation.components.CiviQTextField
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Entry point of the auth flow: email/password sign-in, Google sign-in via
 * Credential Manager, guest mode, and links to registration / password reset.
 */
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(40.dp))

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
            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.action_forgot_password))
            }

            Spacer(modifier = Modifier.height(8.dp))
            CiviQButton(
                text = stringResource(R.string.action_sign_in),
                onClick = { viewModel.onEvent(AuthUiEvent.SignIn) },
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.label_or),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))

            CiviQOutlinedButton(
                text = stringResource(R.string.action_continue_with_google),
                onClick = {
                    coroutineScope.launch {
                        signInWithGoogle(context = context) { idToken ->
                            viewModel.onEvent(AuthUiEvent.SignInWithGoogle(idToken))
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { viewModel.onEvent(AuthUiEvent.ContinueAsGuest) }) {
                Text(stringResource(R.string.action_continue_as_guest))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.label_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(stringResource(R.string.action_sign_up))
                }
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

/**
 * Launches the Credential Manager bottom sheet to retrieve a Google ID token,
 * invoking [onIdToken] on success. Cancellation/no-credential errors are
 * logged and otherwise ignored, leaving the user on the login screen.
 */
private suspend fun signInWithGoogle(context: Context, onIdToken: (String) -> Unit) {
    try {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            onIdToken(googleIdTokenCredential.idToken)
        } else {
            Timber.w("Unexpected credential type returned from Credential Manager")
        }
    } catch (e: GetCredentialException) {
        Timber.i(e, "Google sign-in cancelled or unavailable")
    }
}
