package com.example.assistenttreneren.core.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.example.assistenttreneren.feature.recording.domain.model.RecordingSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class MediaStoreAudioRecorder @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val fileNameFormatter: RecordingFileNameFormatter,
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFileDescriptor: ParcelFileDescriptor? = null
    private var activeRecording: ActiveRecording? = null

    fun startRecording(
        category: String,
        subCategory: String,
        activityId: String?,
    ): ActiveRecording {
        check(mediaRecorder == null) { "Recording is already active." }

        val recordingId = UUID.randomUUID().toString()
        val createdAtMillis = System.currentTimeMillis()
        val displayName = fileNameFormatter.format(
            category = category,
            subCategory = subCategory,
            createdAtMillis = createdAtMillis,
        )
        val uri = createMediaStoreUri(displayName)
        val fileDescriptor = openFileDescriptor(uri)
        val recorder = createRecorder(fileDescriptor)

        return try {
            recorder.prepare()
            recorder.start()

            ActiveRecording(
                recordingId = recordingId,
                activityId = activityId,
                displayName = displayName,
                contentUri = uri,
                category = category,
                subCategory = subCategory,
                startedAtMillis = createdAtMillis,
            ).also {
                mediaRecorder = recorder
                outputFileDescriptor = fileDescriptor
                activeRecording = it
            }
        } catch (throwable: Throwable) {
            recorder.release()
            fileDescriptor.close()
            deleteUri(uri)
            throw throwable
        }
    }

    fun stopRecording(): RecordingSession? {
        val recorder = mediaRecorder ?: return null
        val recording = activeRecording ?: return null

        return try {
            recorder.stop()
            markRecordingComplete(recording.contentUri)

            RecordingSession(
                recordingId = recording.recordingId,
                activityId = recording.activityId,
                displayName = recording.displayName,
                contentUri = recording.contentUri.toString(),
                durationMillis = System.currentTimeMillis() - recording.startedAtMillis,
                category = recording.category,
                subCategory = recording.subCategory,
                createdAtMillis = recording.startedAtMillis,
            )
        } finally {
            recorder.release()
            outputFileDescriptor?.close()
            mediaRecorder = null
            outputFileDescriptor = null
            activeRecording = null
        }
    }

    fun cancelRecording() {
        val recorder = mediaRecorder ?: return
        val recording = activeRecording
        runCatching { recorder.stop() }
        recorder.release()
        outputFileDescriptor?.close()
        recording?.let { deleteUri(it.contentUri) }
        mediaRecorder = null
        outputFileDescriptor = null
        activeRecording = null
    }

    private fun createMediaStoreUri(displayName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(
                MediaStore.Audio.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_MUSIC}/AssistentTreneren",
            )
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        return requireNotNull(
            context.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            ),
        ) {
            "Unable to create recording in MediaStore."
        }
    }

    private fun openFileDescriptor(uri: Uri): ParcelFileDescriptor =
        requireNotNull(
            context.contentResolver.openFileDescriptor(uri, "w"),
        ) {
            "Unable to open output file for recording."
        }

    private fun createRecorder(fileDescriptor: ParcelFileDescriptor): MediaRecorder {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            // Required for API 26-30 because MediaRecorder(context) is only available from API 31.
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64_000)
            setAudioSamplingRate(44_100)
            setOutputFile(fileDescriptor.fileDescriptor)
        }
    }

    private fun markRecordingComplete(uri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.IS_PENDING, 0)
        }
        context.contentResolver.update(uri, contentValues, null, null)
    }

    private fun deleteUri(uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    data class ActiveRecording(
        val recordingId: String,
        val activityId: String?,
        val displayName: String,
        val contentUri: Uri,
        val category: String,
        val subCategory: String,
        val startedAtMillis: Long,
    )
}
