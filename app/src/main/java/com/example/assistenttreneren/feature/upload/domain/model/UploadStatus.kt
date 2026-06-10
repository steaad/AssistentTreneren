package com.example.assistenttreneren.feature.upload.domain.model

enum class UploadStatus {
    Queued,
    Uploading,
    ProcessingAudio,
    Transcribing,
    Completed,
    Failed,
}
