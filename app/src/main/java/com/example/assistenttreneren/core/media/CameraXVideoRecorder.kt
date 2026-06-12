package com.example.assistenttreneren.core.media

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.assistenttreneren.feature.recording.domain.model.RecordingMediaType
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class CameraXVideoRecorder @Inject constructor(
    private val fileNameFormatter: RecordingFileNameFormatter,
) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var activeSession: ActiveVideoRecording? = null

    suspend fun bindPreview(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        val cameraProvider = context.cameraProvider()
        val preview = Preview.Builder().build().also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
        }
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val capture = VideoCapture.withOutput(recorder)

        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            capture,
        )
        camera.cameraControl.setLinearZoom(FIXED_LINEAR_ZOOM)
        videoCapture = capture
    }

    fun startRecording(
        context: Context,
        category: String,
        subCategory: String,
        activityId: String?,
        onStarted: (ActiveVideoRecording) -> Unit,
        onCompleted: (RecordingSession) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        check(activeRecording == null) { "Video recording is already active." }
        val capture = checkNotNull(videoCapture) { "Camera preview is not ready." }
        val recordingId = UUID.randomUUID().toString()
        val createdAtMillis = System.currentTimeMillis()
        val displayName = fileNameFormatter.format(
            category = category,
            subCategory = subCategory,
            createdAtMillis = createdAtMillis,
            extension = VIDEO_EXTENSION,
        )
        val pendingRecording = capture.output.prepareRecording(
            context,
            createOutputOptions(context, displayName),
        )
        val session = ActiveVideoRecording(
            recordingId = recordingId,
            activityId = activityId,
            displayName = displayName,
            category = category,
            subCategory = subCategory,
            startedAtMillis = createdAtMillis,
        )
        activeSession = session
        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> onStarted(session)
                is VideoRecordEvent.Finalize -> {
                    val completedSession = activeSession
                    activeRecording = null
                    activeSession = null
                    if (event.hasError() || completedSession == null) {
                        onError(IllegalStateException(event.cause?.message ?: "Kunne ikke lagre videoopptak."))
                    } else {
                        onCompleted(
                            RecordingSession(
                                recordingId = completedSession.recordingId,
                                activityId = completedSession.activityId,
                                displayName = completedSession.displayName,
                                contentUri = event.outputResults.outputUri.toString(),
                                mediaType = RecordingMediaType.Video,
                                mimeType = VIDEO_MIME_TYPE,
                                durationMillis = System.currentTimeMillis() - completedSession.startedAtMillis,
                                category = completedSession.category,
                                subCategory = completedSession.subCategory,
                                createdAtMillis = completedSession.startedAtMillis,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun stopRecording() {
        activeRecording?.stop()
    }

    fun cancelRecording() {
        activeRecording?.close()
        activeRecording = null
        activeSession = null
    }

    private fun createOutputOptions(
        context: Context,
        displayName: String,
    ): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, VIDEO_MIME_TYPE)
            put(
                MediaStore.Video.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_MOVIES}/AssistentTreneren",
            )
        }

        return MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        ).setContentValues(contentValues).build()
    }

    private suspend fun Context.cameraProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(this)
            future.addListener(
                {
                    try {
                        continuation.resume(future.get())
                    } catch (throwable: Throwable) {
                        continuation.resumeWithException(throwable)
                    }
                },
                ContextCompat.getMainExecutor(this),
            )
        }

    data class ActiveVideoRecording(
        val recordingId: String,
        val activityId: String?,
        val displayName: String,
        val category: String,
        val subCategory: String,
        val startedAtMillis: Long,
    )

    private companion object {
        const val FIXED_LINEAR_ZOOM = 0.6f
        const val VIDEO_EXTENSION = "mp4"
        const val VIDEO_MIME_TYPE = "video/mp4"
    }
}
