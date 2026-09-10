package com.example.assistenttreneren.feature.login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.R
import com.example.assistenttreneren.ui.theme.AssistentTrenerenTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (isBackendBypass: Boolean) -> Unit,
    onInitialPasswordChangeRequired: () -> Unit,
    modifier: Modifier = Modifier,
    sessionMessage: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            val isBackendBypass = uiState.isBackendBypassLogin
            viewModel.consumeLoginSuccess()
            onLoginSuccess(isBackendBypass)
        }
    }

    LaunchedEffect(uiState.isInitialPasswordChangeRequired) {
        if (uiState.isInitialPasswordChangeRequired) {
            viewModel.consumeInitialPasswordChangeRequired()
            onInitialPasswordChangeRequired()
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClicked = viewModel::login,
        onContinueWithoutBackendClicked = viewModel::continueWithoutBackend,
        showBackendBypass = BuildConfig.DEBUG,
        modifier = modifier,
        sessionMessage = sessionMessage,
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onContinueWithoutBackendClicked: () -> Unit,
    showBackendBypass: Boolean,
    modifier: Modifier = Modifier,
    sessionMessage: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                sessionMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    label = {
                        Text(text = stringResource(R.string.login_email_label))
                    },
                    singleLine = true,
                    isError = uiState.emailError != null,
                    supportingText = {
                        uiState.emailError?.let { error ->
                            Text(text = error.asMessage())
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    label = {
                        Text(text = stringResource(R.string.login_password_label))
                    },
                    singleLine = true,
                    isError = uiState.passwordError != null,
                    supportingText = {
                        uiState.passwordError?.let { error ->
                            Text(text = error.asMessage())
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!uiState.isLoading) {
                                onLoginClicked()
                            }
                        },
                    ),
                )

                uiState.loginError?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = error.asMessage(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLoginClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = stringResource(R.string.login_loading))
                        }
                    } else {
                        Text(text = stringResource(R.string.login_button))
                    }
                }

                if (showBackendBypass) {
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onContinueWithoutBackendClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isLoading,
                    ) {
                        Text(text = stringResource(R.string.login_continue_without_backend))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginInputError.asMessage(): String =
    when (this) {
        LoginInputError.Required -> stringResource(R.string.login_error_required)
        LoginInputError.InvalidEmail -> stringResource(R.string.login_error_invalid_email)
        LoginInputError.PasswordTooShort -> stringResource(R.string.login_error_password_too_short)
    }

@Composable
private fun LoginError.asMessage(): String =
    when (this) {
        LoginError.InvalidCredentials -> stringResource(R.string.login_error_invalid_credentials)
        LoginError.NetworkUnavailable -> stringResource(R.string.login_error_network_unavailable)
        LoginError.InvalidServerResponse -> stringResource(R.string.login_error_invalid_server_response)
        LoginError.ValidationError -> stringResource(R.string.login_error_validation)
        is LoginError.ServerError -> stringResource(R.string.login_error_server, code)
        LoginError.Unexpected -> stringResource(R.string.login_error_unexpected)
    }

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    AssistentTrenerenTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "coach@example.com",
                password = "password",
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onContinueWithoutBackendClicked = {},
            showBackendBypass = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentLoadingPreview() {
    AssistentTrenerenTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "coach@example.com",
                password = "password",
                isLoading = true,
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onContinueWithoutBackendClicked = {},
            showBackendBypass = true,
        )
    }
}
