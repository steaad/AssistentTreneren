package com.example.assistenttreneren.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.assistenttreneren.R
import com.example.assistenttreneren.core.auth.UserRole
import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, catalogViewModel: LearningCatalogViewModel, onNavigateBack: () -> Unit, onUnauthorized: () -> Unit, modifier: Modifier = Modifier) {
    val users by viewModel.uiState.collectAsState(); val catalog by catalogViewModel.uiState.collectAsState(); var tab by remember { mutableStateOf(0) }
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back_button))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
    }) { padding -> Column(Modifier.padding(padding).padding(24.dp).fillMaxSize()) {
        if (!users.isAdministrator) { Text(stringResource(R.string.settings_no_options)); return@Column }
        TabRow(tab) { Tab(tab == 0, { tab = 0 }, text = { Text("Brukere") }); Tab(tab == 1, { tab = 1 }, text = { Text("Læringskatalog") }) }
        Spacer(Modifier.height(16.dp)); if (tab == 0) UserTab(users, viewModel, onUnauthorized) else CatalogTab(catalog, catalogViewModel)
    } }
}

@Composable private fun UserTab(state: SettingsUiState, vm: SettingsViewModel, onUnauthorized: () -> Unit) = Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text(stringResource(R.string.create_user_title), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.create_user_description))
    OutlinedTextField(state.email, vm::onEmailChanged, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.create_user_email)) }); OutlinedTextField(state.displayName, vm::onDisplayNameChanged, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.create_user_display_name)) })
    RoleSelector(state.role, !state.isSubmitting, vm::onRoleChanged); OutlinedTextField(state.temporaryPassword, vm::onTemporaryPasswordChanged, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.create_user_temporary_password)) }, visualTransformation = PasswordVisualTransformation()); OutlinedTextField(state.confirmTemporaryPassword, vm::onConfirmTemporaryPasswordChanged, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.create_user_confirm_password)) }, visualTransformation = PasswordVisualTransformation())
    state.message?.let { Text(it.asText(), color = if (it == SettingsMessage.Created) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }; Button({ vm.createUser(onUnauthorized) }, Modifier.fillMaxWidth(), enabled = !state.isSubmitting) { if (state.isSubmitting) CircularProgressIndicator() else Text(stringResource(R.string.create_user_submit)) }
}

@Composable private fun CatalogTab(state: LearningCatalogUiState, vm: LearningCatalogViewModel) {
    var dialog by remember { mutableStateOf<CatalogAction?>(null) }; var selectedSubtheme by remember(state.selectedTheme?.id) { mutableStateOf<LearningCatalogItem?>(null) }
    val themes = state.themes.filter { it.teamFunction == state.selectedTeamFunction }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Læringskatalog", style = MaterialTheme.typography.titleLarge); if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        TeamFunctionPicker(state.selectedTeamFunction, state.teamFunctions, vm::selectTeamFunction)
        state.selectedTeamFunction?.let {
            Button({ dialog = CatalogAction.CreateTheme }, Modifier.fillMaxWidth()) { Text("Opprett hovedtema") }; Text("Hovedtema", style = MaterialTheme.typography.titleMedium)
            themes.forEach { item -> ItemRow(item, item == state.selectedTheme, { vm.selectTheme(item) }, { dialog = CatalogAction.EditTheme(item) }, { dialog = CatalogAction.DeleteTheme(item) }) }
        }
        state.selectedTheme?.let { theme ->
            Button({ dialog = CatalogAction.CreateSubtheme }, Modifier.fillMaxWidth()) { Text("Opprett undertema") }; Button({ dialog = CatalogAction.CreateObjective(selectedSubtheme) }, Modifier.fillMaxWidth()) { Text("Opprett læringsmål") }
            Text("Undertema", style = MaterialTheme.typography.titleMedium); OutlinedButton({ selectedSubtheme = null }, Modifier.fillMaxWidth()) { Text("Hovedtema: ${theme.name}") }
            state.subthemes.forEach { item -> ItemRow(item, item == selectedSubtheme, { selectedSubtheme = item }, { dialog = CatalogAction.EditSubtheme(item) }, { dialog = CatalogAction.DeleteSubtheme(item) }) }
            Text("Læringsmål", style = MaterialTheme.typography.titleMedium)
            CatalogObjectiveGroup(
                title = "Direkte under ${theme.name}",
                objectives = state.objectives.filter { it.parentId == null || it.parentId == theme.id },
                onEdit = { dialog = CatalogAction.EditObjective(it) },
                onDelete = { dialog = CatalogAction.DeleteObjective(it) },
            )
            state.subthemes.forEach { subtheme ->
                CatalogObjectiveGroup(
                    title = "Under ${subtheme.name}",
                    objectives = state.objectives.filter { it.parentId == subtheme.id },
                    onEdit = { dialog = CatalogAction.EditObjective(it) },
                    onDelete = { dialog = CatalogAction.DeleteObjective(it) },
                )
            }
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    dialog?.let { ActionDialog(it, state.selectedTeamFunction, vm) { dialog = null } }
}

@Composable
private fun CatalogObjectiveGroup(
    title: String,
    objectives: List<LearningCatalogItem>,
    onEdit: (LearningCatalogItem) -> Unit,
    onDelete: (LearningCatalogItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (objectives.isEmpty()) {
            Text("Ingen læringsmål.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            objectives.forEach { item -> ItemRow(item, false, {}, { onEdit(item) }, { onDelete(item) }) }
        }
    }
}

@Composable private fun ItemRow(item: LearningCatalogItem, selected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) = OutlinedCard(onClick = onSelect, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.name, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface); item.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) } }; IconButton(onEdit) { Icon(Icons.Outlined.Edit, null) }; IconButton(onDelete) { Icon(Icons.Outlined.Delete, null) } } }

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun TeamFunctionPicker(selected: TeamFunction?, values: List<TeamFunction>, onSelected: (TeamFunction?) -> Unit) { var expanded by remember { mutableStateOf(false) }; ExposedDropdownMenuBox(expanded, { expanded = !expanded }) { OutlinedTextField(selected?.label() ?: "Velg lagfunksjon", {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("Lagfunksjon") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }); ExposedDropdownMenu(expanded, { expanded = false }) { values.forEach { value -> DropdownMenuItem({ Text(value.label()) }, { onSelected(value); expanded = false }) } } } }

private sealed interface CatalogAction { data object CreateTheme : CatalogAction; data object CreateSubtheme : CatalogAction; data class CreateObjective(val subtheme: LearningCatalogItem?) : CatalogAction; data class EditTheme(val item: LearningCatalogItem) : CatalogAction; data class EditSubtheme(val item: LearningCatalogItem) : CatalogAction; data class EditObjective(val item: LearningCatalogItem) : CatalogAction; data class DeleteTheme(val item: LearningCatalogItem) : CatalogAction; data class DeleteSubtheme(val item: LearningCatalogItem) : CatalogAction; data class DeleteObjective(val item: LearningCatalogItem) : CatalogAction }

@Composable private fun ActionDialog(action: CatalogAction, function: TeamFunction?, vm: LearningCatalogViewModel, dismiss: () -> Unit) { when (action) { is CatalogAction.DeleteTheme -> ConfirmDelete(action.item.name, { vm.deleteTheme(action.item); dismiss() }, dismiss); is CatalogAction.DeleteSubtheme -> ConfirmDelete(action.item.name, { vm.deleteSubtheme(action.item); dismiss() }, dismiss); is CatalogAction.DeleteObjective -> ConfirmDelete(action.item.name, { vm.deleteObjective(action.item); dismiss() }, dismiss); else -> Editor(action, function, vm, dismiss) } }
@Composable private fun ConfirmDelete(name: String, confirm: () -> Unit, dismiss: () -> Unit) = AlertDialog(onDismissRequest = dismiss, title = { Text("Slett $name?") }, text = { Text("Verdien slettes bare dersom backend tillater det.") }, confirmButton = { Button(confirm) { Text("Slett") } }, dismissButton = { OutlinedButton(dismiss) { Text("Avbryt") } })
@Composable private fun Editor(action: CatalogAction, function: TeamFunction?, vm: LearningCatalogViewModel, dismiss: () -> Unit) { val item = when (action) { is CatalogAction.EditTheme -> action.item; is CatalogAction.EditSubtheme -> action.item; is CatalogAction.EditObjective -> action.item; else -> null }; var name by remember { mutableStateOf(item?.name.orEmpty()) }; var description by remember { mutableStateOf(item?.description.orEmpty()) }; AlertDialog(onDismissRequest = dismiss, title = { Text(if (item == null) "Opprett verdi" else "Endre verdi") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Navn") }); OutlinedTextField(description, { description = it }, label = { Text("Beskrivelse (valgfri)") }) } }, confirmButton = { Button({ when (action) { CatalogAction.CreateTheme -> function?.let { vm.createTheme(name, description) }; CatalogAction.CreateSubtheme -> vm.createSubtheme(name, description); is CatalogAction.CreateObjective -> vm.createObjective(name, description, action.subtheme); is CatalogAction.EditTheme -> vm.updateTheme(action.item, name, description); is CatalogAction.EditSubtheme -> vm.updateSubtheme(action.item, name, description); is CatalogAction.EditObjective -> vm.updateObjective(action.item, name, description); else -> Unit }; dismiss() }, enabled = name.isNotBlank()) { Text("Lagre") } }, dismissButton = { OutlinedButton(dismiss) { Text("Avbryt") } }) }

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun RoleSelector(role: UserRole, enabled: Boolean, selected: (UserRole) -> Unit) { var expanded by remember { mutableStateOf(false) }; ExposedDropdownMenuBox(expanded, { if (enabled) expanded = !expanded }) { OutlinedTextField(role.label(), {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text(stringResource(R.string.create_user_role)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }); ExposedDropdownMenu(expanded, { expanded = false }) { UserRole.entries.forEach { value -> DropdownMenuItem({ Text(value.label()) }, { selected(value); expanded = false }) } } } }
@Composable private fun UserRole.label() = if (this == UserRole.ADMINISTRATOR) stringResource(R.string.user_role_administrator) else stringResource(R.string.user_role_trainer)
private fun TeamFunction.label() = when (this) { TeamFunction.ATTACK -> "Angrep"; TeamFunction.DEFENCE -> "Forsvar"; TeamFunction.ATTACK_TRANSITION -> "Angrepsovergang"; TeamFunction.DEFENCE_TRANSITION -> "Forsvarsovergang"; TeamFunction.OFFENSIVE_SET_PIECES -> "Offensive dødballer"; TeamFunction.DEFENSIVE_SET_PIECES -> "Defensive dødballer" }
@Composable private fun SettingsMessage.asText() = when (this) { SettingsMessage.Created -> stringResource(R.string.create_user_success); SettingsMessage.ValidationError -> stringResource(R.string.create_user_error_validation); SettingsMessage.PasswordsDoNotMatch -> stringResource(R.string.create_user_error_not_matching); SettingsMessage.PasswordTooShort -> stringResource(R.string.create_user_error_too_short); SettingsMessage.EmailAlreadyExists -> stringResource(R.string.create_user_error_email_exists); SettingsMessage.NetworkUnavailable -> stringResource(R.string.login_error_network_unavailable); SettingsMessage.Forbidden -> stringResource(R.string.create_user_error_forbidden); SettingsMessage.Unexpected -> stringResource(R.string.login_error_unexpected) }
