package com.example.assistenttreneren.feature.activitywizard.presentation.steps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardStep
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardUiState
import com.example.assistenttreneren.feature.activitywizard.presentation.components.CoachActivityWizardScaffold
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSubCategory
import com.example.assistenttreneren.feature.recording.presentation.RecordingUiState
import com.example.assistenttreneren.feature.recording.presentation.RecordingViewModel
import kotlinx.coroutines.delay

@Composable
fun AudioRecordingStepScreen(
    uiState: CoachActivityWizardUiState,
    onStepOpened: (CoachActivityWizardStep) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    recordingViewModel: RecordingViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        onStepOpened(CoachActivityWizardStep.AudioRecording)
    }

    val recordingUiState by recordingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val category = uiState.activityCategory
        ?: uiState.selectedExistingActivity?.activityCategory
    val subCategories = RecordingSubCategory.forActivityCategory(category)
    val activityId = uiState.selectedActivityId
    var permissionMessageVisible by remember { mutableStateOf(false) }
    var shouldStartAfterPermissionGrant by remember { mutableStateOf(false) }
    var matchClockElapsedMillis by rememberSaveable { mutableLongStateOf(0L) }
    var matchClockStartedAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var matchClockPeriod by rememberSaveable { mutableStateOf<String?>(null) }
    var matchPeriodStartedAtMatchClockMillis by rememberSaveable { mutableLongStateOf(0L) }
    var showStopMatchClockDialog by rememberSaveable { mutableStateOf(false) }
    var isVideoPreviewExpanded by rememberSaveable { mutableStateOf(false) }

    fun currentMatchClockMillis(): Long? = matchClockStartedAtMillis?.let { startedAt ->
        (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
    }

    LaunchedEffect(matchClockStartedAtMillis) {
        while (matchClockStartedAtMillis != null) {
            delay(1_000L)
            val startedAtMillis = matchClockStartedAtMillis ?: continue
            matchClockElapsedMillis = (System.currentTimeMillis() - startedAtMillis).coerceAtLeast(0L)
        }
    }

    LaunchedEffect(category, recordingUiState.subCategory) {
        val selectedSubCategoryIsValid = subCategories.any { subCategory ->
            subCategory.displayName == recordingUiState.subCategory
        }

        if (recordingUiState.subCategory.isNotBlank() && !selectedSubCategoryIsValid) {
            recordingViewModel.clearSubCategory()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val hasRequiredPermissions = requiredRecordingPermissions(
            recordingUiState.selectedMediaType,
        ).all { permission ->
            grants[permission] == true || ContextCompat.checkSelfPermission(
                context,
                permission,
            ) == PackageManager.PERMISSION_GRANTED
        }

        permissionMessageVisible = !hasRequiredPermissions
        if (hasRequiredPermissions && shouldStartAfterPermissionGrant && category != null) {
            when (recordingUiState.selectedMediaType) {
                RecordingMediaType.Audio -> recordingViewModel.startAudioRecording(
                    category = category,
                    activityId = activityId,
                    matchPeriod = matchClockPeriod,
                    matchClockStartMillis = currentMatchClockMillis(),
                )

                RecordingMediaType.Video -> recordingViewModel.startVideoRecording(
                    context = context,
                    category = category,
                    activityId = activityId,
                    matchPeriod = matchClockPeriod,
                    matchClockStartMillis = currentMatchClockMillis(),
                )

                null -> Unit
            }
        }
        shouldStartAfterPermissionGrant = false
    }

    CoachActivityWizardScaffold(
        title = stringResource(R.string.wizard_audio_recording_title),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateNext = onNavigateNext,
        isBackEnabled = !recordingUiState.isRecording &&
            matchClockStartedAtMillis == null &&
            matchClockElapsedMillis == 0L,
        isNextEnabled = !recordingUiState.isRecording,
        contentFillsAvailableSpace = recordingUiState.selectedMediaType == RecordingMediaType.Video,
    ) {
        Column(
            modifier = if (recordingUiState.selectedMediaType == RecordingMediaType.Video) {
                Modifier.fillMaxSize()
            } else {
                Modifier.fillMaxWidth()
            },
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!isVideoPreviewExpanded) {
                RecordingMediaTypeSegmentedButtons(
                    selectedMediaType = recordingUiState.selectedMediaType,
                    enabled = !recordingUiState.isRecording,
                    onMediaTypeSelected = recordingViewModel::onMediaTypeSelected,
                )

                SubCategorySegmentedButtons(
                    subCategories = subCategories,
                    selectedSubCategory = recordingUiState.subCategory,
                    enabled = !recordingUiState.isRecording &&
                        (category != "Kamp" || matchClockStartedAtMillis == null),
                    onSubCategorySelected = { subCategory ->
                        if (category == "Kamp" && matchClockStartedAtMillis != null) {
                            return@SubCategorySegmentedButtons
                        }
                        val selectedPeriod = subCategory.toMatchPeriod()
                        if (selectedPeriod != matchClockPeriod) {
                            matchClockPeriod = selectedPeriod
                            matchPeriodStartedAtMatchClockMillis = matchClockElapsedMillis
                            matchClockStartedAtMillis = null
                        }
                        recordingViewModel.onSubCategorySelected(subCategory)
                    },
                )

                if (category == "Kamp") {
                    MatchClockPanel(
                        elapsedMillis = matchClockElapsedMillis,
                        periodElapsedMillis = (matchClockElapsedMillis - matchPeriodStartedAtMatchClockMillis)
                            .coerceAtLeast(0L),
                        isRunning = matchClockStartedAtMillis != null,
                        matchPeriodLabel = matchClockPeriod?.toMatchPeriodLabel(),
                        halfDurationMinutes = uiState.matchHalfDurationMinutes,
                        initiallyExpanded = recordingUiState.selectedMediaType != RecordingMediaType.Video,
                        isStartEnabled = recordingUiState.subCategory.isNotBlank(),
                        onStart = {
                            if (recordingUiState.subCategory.isNotBlank() && matchClockStartedAtMillis == null) {
                                matchClockStartedAtMillis = System.currentTimeMillis() - matchClockElapsedMillis
                            }
                        },
                        onStop = { showStopMatchClockDialog = true },
                    )
                }
            }

            when (recordingUiState.selectedMediaType) {
                RecordingMediaType.Audio -> RecordingControlPanel(
                    recordingUiState = recordingUiState,
                    permissionMessageVisible = permissionMessageVisible,
                    canStartRecording = category != null && canStartMatchRecording(category, recordingUiState.subCategory, matchClockStartedAtMillis != null),
                    onStartRecording = startRecording@{
                        if (category == null) {
                            permissionMessageVisible = true
                            return@startRecording
                        }

                        if (hasRecordingPermissions(context, RecordingMediaType.Audio)) {
                            recordingViewModel.startAudioRecording(
                                category = category,
                                activityId = activityId,
                                matchPeriod = matchClockPeriod,
                                matchClockStartMillis = currentMatchClockMillis(),
                            )
                        } else {
                            shouldStartAfterPermissionGrant = true
                            permissionLauncher.launch(
                                requiredRecordingPermissions(RecordingMediaType.Audio),
                            )
                        }
                    },
                    onStopRecording = recordingViewModel::stopRecording,
                )

                RecordingMediaType.Video -> VideoRecordingPanel(
                    recordingUiState = recordingUiState,
                    permissionMessageVisible = permissionMessageVisible,
                    canStartRecording = category != null && canStartMatchRecording(category, recordingUiState.subCategory, matchClockStartedAtMillis != null),
                    modifier = Modifier.fillMaxWidth(),
                    isPreviewExpanded = isVideoPreviewExpanded,
                    onPreviewExpansionChanged = { isVideoPreviewExpanded = it },
                    onBindPreview = { previewView ->
                        recordingViewModel.bindVideoPreview(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                        )
                    },
                    onStartRecording = startRecording@{
                        if (category == null) {
                            permissionMessageVisible = true
                            return@startRecording
                        }

                        if (hasRecordingPermissions(context, RecordingMediaType.Video)) {
                            recordingViewModel.startVideoRecording(
                                context = context,
                                category = category,
                                activityId = activityId,
                            )
                        } else {
                            shouldStartAfterPermissionGrant = true
                            permissionLauncher.launch(
                                requiredRecordingPermissions(RecordingMediaType.Video),
                            )
                        }
                    },
                    onStopRecording = recordingViewModel::stopRecording,
                )

                null -> Text(
                    text = stringResource(R.string.recording_media_type_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showStopMatchClockDialog) {
        AlertDialog(
            onDismissRequest = { showStopMatchClockDialog = false },
            title = { Text("Stopp kampuret?") },
            text = { Text("Er du sikker på at du vil stoppe kampuret? Dette gjøres normalt bare når det er pause eller at kampen er ferdigspilt.") },
            confirmButton = {
                Button(onClick = {
                    matchClockElapsedMillis = currentMatchClockMillis() ?: matchClockElapsedMillis
                    matchClockStartedAtMillis = null
                    matchClockPeriod = null
                    matchPeriodStartedAtMatchClockMillis = matchClockElapsedMillis
                    recordingViewModel.clearSubCategory()
                    showStopMatchClockDialog = false
                }) { Text("Stopp") }
            },
            dismissButton = { OutlinedButton(onClick = { showStopMatchClockDialog = false }) { Text("Avbryt") } },
        )
    }

    if (isVideoPreviewExpanded) {
        Dialog(onDismissRequest = { isVideoPreviewExpanded = false }) {
            Surface(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { previewContext ->
                            PreviewView(previewContext).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                recordingViewModel.bindVideoPreview(context, lifecycleOwner, this)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    Button(
                        onClick = { isVideoPreviewExpanded = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = null)
                        Text("Minimer")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingMediaTypeSegmentedButtons(
    selectedMediaType: RecordingMediaType?,
    enabled: Boolean,
    onMediaTypeSelected: (RecordingMediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mediaTypes = listOf(
        RecordingMediaType.Audio to stringResource(R.string.recording_media_type_audio),
        RecordingMediaType.Video to stringResource(R.string.recording_media_type_video),
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        mediaTypes.forEachIndexed { index, (mediaType, label) ->
            SegmentedButton(
                selected = selectedMediaType == mediaType,
                onClick = { onMediaTypeSelected(mediaType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = mediaTypes.size,
                ),
                enabled = enabled,
                icon = {},
                label = {
                    SegmentedButtonText(text = label)
                },
            )
        }
    }
}

@Composable
private fun MatchClockPanel(
    elapsedMillis: Long,
    periodElapsedMillis: Long,
    isRunning: Boolean,
    matchPeriodLabel: String?,
    halfDurationMinutes: Int,
    initiallyExpanded: Boolean,
    isStartEnabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    var expanded by rememberSaveable("match-clock-$initiallyExpanded") { mutableStateOf(initiallyExpanded) }
    val halfDurationMillis = halfDurationMinutes * 60_000L
    val halfElapsedMillis = periodElapsedMillis.coerceAtMost(halfDurationMillis)
    val overtimeElapsedMillis = (periodElapsedMillis - halfDurationMillis).coerceAtLeast(0L)
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Kamptid", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatMatchClock(halfElapsedMillis),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (overtimeElapsedMillis > 0L) {
                            Text(" - ", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = formatMatchClock(overtimeElapsedMillis),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    matchPeriodLabel?.let { label ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = if (expanded) "Skjul kamptid" else "Vis kamptid")
                }
            }
            if (expanded) {
                Text(
                    text = formatStopwatchTime(elapsedMillis),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 48.sp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onStart, enabled = isStartEnabled && !isRunning) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Text("Start")
                }
                OutlinedButton(onClick = onStop, enabled = isRunning) {
                    Icon(Icons.Outlined.Stop, contentDescription = null)
                    Text("Stopp")
                }
            }
            }
        }
    }
}

@Composable
private fun VideoRecordingPanel(
    recordingUiState: RecordingUiState,
    permissionMessageVisible: Boolean,
    canStartRecording: Boolean,
    isPreviewExpanded: Boolean,
    onPreviewExpansionChanged: (Boolean) -> Unit,
    onBindPreview: (PreviewView) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        onBindPreview(this)
                    }
                },
                modifier = Modifier.fillMaxSize().alpha(0f),
            )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecordingCaptureIndicator(
                icon = Icons.Outlined.Videocam,
                isRecording = recordingUiState.isRecording,
                activeText = "VIDEOOPPTAK PÅGÅR",
            )

            OutlinedButton(onClick = { onPreviewExpansionChanged(true) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Maksimer")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStartRecording,
                    enabled = recordingUiState.canStartRecording && canStartRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_start_button))
                }

                OutlinedButton(
                    onClick = onStopRecording,
                    enabled = recordingUiState.canStopRecording,
                    colors = activeStopButtonColors(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_stop_button))
                }
            }

            RecordingStatusText(
                recordingUiState = recordingUiState,
                permissionMessageVisible = permissionMessageVisible,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        }
    }
}

@Composable
private fun RecordingControlPanel(
    recordingUiState: RecordingUiState,
    permissionMessageVisible: Boolean,
    canStartRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecordingCaptureIndicator(Icons.Outlined.Mic, recordingUiState.isRecording, "OPPTAK PÅGÅR")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onStartRecording,
                    enabled = recordingUiState.canStartRecording && canStartRecording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_start_button))
                }

                OutlinedButton(
                    onClick = onStopRecording,
                    enabled = recordingUiState.canStopRecording,
                    colors = activeStopButtonColors(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.recording_stop_button))
                }
            }

            RecordingStatusText(
                recordingUiState = recordingUiState,
                permissionMessageVisible = permissionMessageVisible,
            )
        }
    }
}

@Composable
private fun RecordingCaptureIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isRecording: Boolean,
    activeText: String,
) {
    val transition = rememberInfiniteTransition(label = "recording-microphone-pulse")
    val ringScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "recording-microphone-ring-scale",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "recording-microphone-ring-alpha",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isRecording) {
                Surface(
                    modifier = Modifier.size(56.dp).scale(ringScale),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = ringAlpha),
                ) {}
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
        }
        if (isRecording) {
            Text(
                text = "● $activeText",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SubCategorySegmentedButtons(
    subCategories: List<RecordingSubCategory>,
    selectedSubCategory: String,
    enabled: Boolean,
    onSubCategorySelected: (RecordingSubCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (subCategories.isEmpty()) {
        return
    }

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        subCategories.forEachIndexed { index, subCategory ->
            SegmentedButton(
                selected = subCategory.displayName == selectedSubCategory,
                onClick = { onSubCategorySelected(subCategory) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = subCategories.size,
                ),
                enabled = enabled,
                icon = {},
                label = {
                    SegmentedButtonText(text = subCategory.displayName)
                },
            )
        }
    }
}

@Composable
private fun SegmentedButtonText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun activeStopButtonColors() =
    ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

@Composable
private fun RecordingStatusText(
    recordingUiState: com.example.assistenttreneren.feature.recording.presentation.RecordingUiState,
    permissionMessageVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val text = when {
        permissionMessageVisible -> stringResource(R.string.recording_permission_required)
        recordingUiState.errorMessage != null -> recordingUiState.errorMessage
        recordingUiState.isRecording -> stringResource(
            R.string.recording_active_status,
            recordingUiState.activeDisplayName.orEmpty(),
        )
        recordingUiState.completedRecording != null -> stringResource(
            R.string.recording_completed_status,
            recordingUiState.completedRecording.displayName,
        )
        else -> stringResource(R.string.recording_idle_status)
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun hasRecordingPermissions(
    context: android.content.Context,
    mediaType: RecordingMediaType,
): Boolean =
    requiredRecordingPermissions(mediaType).all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

private fun requiredRecordingPermissions(mediaType: RecordingMediaType?): Array<String> =
    buildList {
        when (mediaType) {
            RecordingMediaType.Audio -> add(Manifest.permission.RECORD_AUDIO)
            RecordingMediaType.Video -> add(Manifest.permission.CAMERA)
            null -> Unit
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (mediaType == RecordingMediaType.Audio) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }.toTypedArray()

private fun canStartMatchRecording(
    category: String?,
    subCategory: String,
    isMatchClockRunning: Boolean,
): Boolean =
    category != "Kamp" || subCategory !in setOf("1.omgang", "2.omgang") || isMatchClockRunning

private fun RecordingSubCategory.toMatchPeriod(): String? = when (displayName) {
    "1.omgang" -> "FIRST_HALF"
    "2.omgang" -> "SECOND_HALF"
    else -> null
}

private fun String.toMatchPeriodLabel(): String = when (this) {
    "FIRST_HALF" -> "1.omg"
    "SECOND_HALF" -> "2.omg"
    else -> this
}

private fun formatStopwatchTime(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return "%02d : %02d : %02d".format(hours, minutes, seconds)
}

private fun formatMatchClock(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1_000L
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}
