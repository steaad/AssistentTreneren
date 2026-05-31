package com.example.assistenttreneren.core.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.assistenttreneren.MainActivity
import com.example.assistenttreneren.R
import com.example.assistenttreneren.feature.recording.domain.model.RecordingStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {
    @Inject
    lateinit var audioRecorder: MediaStoreAudioRecorder

    @Inject
    lateinit var stateHolder: AudioRecordingStateHolder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording(intent)
            ACTION_STOP -> stopRecording()
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        audioRecorder.cancelRecording()
        super.onDestroy()
    }

    private fun startRecording(intent: Intent) {
        startAsForeground(getString(R.string.recording_notification_starting))

        val category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty()
        val subCategory = intent.getStringExtra(EXTRA_SUB_CATEGORY).orEmpty()
        val activityId = intent.getStringExtra(EXTRA_ACTIVITY_ID)

        if (category.isBlank() || subCategory.isBlank()) {
            stateHolder.update(
                RecordingStatus.Error(getString(R.string.recording_error_missing_name_parts)),
            )
            stopSelf()
            return
        }

        try {
            val activeRecording = audioRecorder.startRecording(
                category = category,
                subCategory = subCategory,
                activityId = activityId,
            )
            stateHolder.update(
                RecordingStatus.Recording(
                    recordingId = activeRecording.recordingId,
                    displayName = activeRecording.displayName,
                    startedAtMillis = activeRecording.startedAtMillis,
                ),
            )
            notificationManager.notify(
                NOTIFICATION_ID,
                createNotification(activeRecording.displayName),
            )
        } catch (throwable: Throwable) {
            stateHolder.update(
                RecordingStatus.Error(
                    throwable.message ?: getString(R.string.recording_error_start_failed),
                ),
            )
            stopSelf()
        }
    }

    private fun stopRecording() {
        val completedSession = runCatching {
            audioRecorder.stopRecording()
        }.getOrElse { throwable ->
            stateHolder.update(
                RecordingStatus.Error(
                    throwable.message ?: getString(R.string.recording_error_stop_failed),
                ),
            )
            null
        }

        completedSession?.let { session ->
            stateHolder.update(RecordingStatus.Completed(session))
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startAsForeground(contentText: String) {
        val notification = createNotification(contentText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(contentText: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_recording)
            .setContentTitle(getString(R.string.recording_notification_title))
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(createContentIntent())
            .addAction(
                0,
                getString(R.string.recording_stop_button),
                createStopIntent(),
            )
            .build()

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            CONTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createStopIntent(): PendingIntent {
        val intent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            STOP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.recording_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.recording_notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    companion object {
        private const val ACTION_START = "com.example.assistenttreneren.recording.START"
        private const val ACTION_STOP = "com.example.assistenttreneren.recording.STOP"
        private const val EXTRA_CATEGORY = "extra_category"
        private const val EXTRA_SUB_CATEGORY = "extra_sub_category"
        private const val EXTRA_ACTIVITY_ID = "extra_activity_id"
        private const val CHANNEL_ID = "audio_recording"
        private const val NOTIFICATION_ID = 1101
        private const val CONTENT_REQUEST_CODE = 2101
        private const val STOP_REQUEST_CODE = 2102

        fun startIntent(
            context: Context,
            category: String,
            subCategory: String,
            activityId: String?,
        ): Intent =
            Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CATEGORY, category)
                putExtra(EXTRA_SUB_CATEGORY, subCategory)
                putExtra(EXTRA_ACTIVITY_ID, activityId)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
