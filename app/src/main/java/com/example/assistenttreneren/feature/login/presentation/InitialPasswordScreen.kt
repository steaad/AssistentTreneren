package com.example.assistenttreneren.feature.login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.R
import androidx.compose.ui.res.stringResource

@Composable
fun InitialPasswordScreen(
    viewModel: LoginViewModel,
    onPasswordChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.initialPasswordUiState.collectAsState()
    LaunchedEffect(state.isPasswordChangeSuccessful) {
        if (state.isPasswordChangeSuccessful) {
            viewModel.consumeInitialPasswordChangeSuccess()
            onPasswordChanged()
        }
    }
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(96.dp))
            Text(stringResource(R.string.initial_password_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.initial_password_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                singleLine = true,
                label = { Text(stringResource(R.string.initial_password_new_label)) },
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmNewPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                singleLine = true,
                label = { Text(stringResource(R.string.initial_password_confirm_label)) },
                visualTransformation = PasswordVisualTransformation(),
            )
            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(initialPasswordErrorText(error), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = viewModel::changeInitialPassword, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (state.isLoading) CircularProgressIndicator() else Text(stringResource(R.string.initial_password_submit))
            }
        }
    }
}

@Composable
private fun initialPasswordErrorText(error: InitialPasswordError): String = when (error) {
    InitialPasswordError.MissingCredentials -> stringResource(R.string.initial_password_error_missing_credentials)
    InitialPasswordError.PasswordTooShort -> stringResource(R.string.initial_password_error_too_short)
    InitialPasswordError.PasswordsDoNotMatch -> stringResource(R.string.initial_password_error_not_matching)
    InitialPasswordError.PasswordMustDiffer -> stringResource(R.string.initial_password_error_must_differ)
    InitialPasswordError.InvalidCredentials -> stringResource(R.string.initial_password_error_invalid_credentials)
    InitialPasswordError.ValidationError -> stringResource(R.string.initial_password_error_validation)
    InitialPasswordError.NetworkUnavailable -> stringResource(R.string.login_error_network_unavailable)
    InitialPasswordError.Unexpected -> stringResource(R.string.login_error_unexpected)
}
