package com.example.assistenttreneren.feature.activitywizard.presentation

enum class CoachActivityWizardStep(
    val number: Int,
) {
    ActivityType(number = 1),
    AudioRecording(number = 2),
    Upload(number = 3),
    Summary(number = 4),
}
