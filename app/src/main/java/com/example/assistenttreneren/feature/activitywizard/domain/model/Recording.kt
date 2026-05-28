package com.example.assistenttreneren.feature.activitywizard.domain.model

data class Recording(
    val id: String,
    val recordingType: String,
    val filename: String,
    val duration: Long,
)
