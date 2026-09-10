package com.example.assistenttreneren.feature.settings.presentation

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.R
import com.example.assistenttreneren.core.auth.UserRole

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onUnauthorized: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.navigate_back_button)) }
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
            if (!state.isAdministrator) {
                Text(stringResource(R.string.settings_no_options), color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Text(stringResource(R.string.create_user_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.create_user_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(state.email, viewModel::onEmailChanged, Modifier.fillMaxWidth(), enabled = !state.isSubmitting, label = { Text(stringResource(R.string.create_user_email)) }, singleLine = true)
            OutlinedTextField(state.displayName, viewModel::onDisplayNameChanged, Modifier.fillMaxWidth(), enabled = !state.isSubmitting, label = { Text(stringResource(R.string.create_user_display_name)) }, singleLine = true)
            RoleSelector(state.role, !state.isSubmitting, viewModel::onRoleChanged)
            OutlinedTextField(state.temporaryPassword, viewModel::onTemporaryPasswordChanged, Modifier.fillMaxWidth(), enabled = !state.isSubmitting, label = { Text(stringResource(R.string.create_user_temporary_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(state.confirmTemporaryPassword, viewModel::onConfirmTemporaryPasswordChanged, Modifier.fillMaxWidth(), enabled = !state.isSubmitting, label = { Text(stringResource(R.string.create_user_confirm_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            state.message?.let { message ->
                Text(
                    text = message.asText(),
                    color = if (message == SettingsMessage.Created) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.createUser(onUnauthorized) }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (state.isSubmitting) CircularProgressIndicator() else Text(stringResource(R.string.create_user_submit))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleSelector(selectedRole: UserRole, enabled: Boolean, onRoleChanged: (UserRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = !expanded }) {
        OutlinedTextField(
            value = selectedRole.label(), onValueChange = {}, readOnly = true, enabled = enabled,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.create_user_role)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(text = { Text(role.label()) }, onClick = { onRoleChanged(role); expanded = false })
            }
        }
    }
}

@Composable
private fun UserRole.label(): String = when (this) {
    UserRole.ADMINISTRATOR -> stringResource(R.string.user_role_administrator)
    UserRole.TRAINER -> stringResource(R.string.user_role_trainer)
}

@Composable
private fun SettingsMessage.asText(): String = when (this) {
    SettingsMessage.Created -> stringResource(R.string.create_user_success)
    SettingsMessage.ValidationError -> stringResource(R.string.create_user_error_validation)
    SettingsMessage.PasswordsDoNotMatch -> stringResource(R.string.create_user_error_not_matching)
    SettingsMessage.PasswordTooShort -> stringResource(R.string.create_user_error_too_short)
    SettingsMessage.EmailAlreadyExists -> stringResource(R.string.create_user_error_email_exists)
    SettingsMessage.NetworkUnavailable -> stringResource(R.string.login_error_network_unavailable)
    SettingsMessage.Forbidden -> stringResource(R.string.create_user_error_forbidden)
    SettingsMessage.Unexpected -> stringResource(R.string.login_error_unexpected)
}
