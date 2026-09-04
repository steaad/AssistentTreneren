package com.example.assistenttreneren.feature.transcription.data.mapper

import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventIssueDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionReviewDto
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEvent
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventIssue
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionRecording
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionReview
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventRequestDto

fun TranscriptionReviewDto.toTranscriptionReview(): TranscriptionReview = TranscriptionReview(
    activityId = activityId,
    recordings = recordings.map { recording ->
        TranscriptionRecording(
            recordingId = recording.recordingId,
            backendRecordingId = recording.backendRecordingId,
            filename = recording.filename,
            mediaType = recording.mediaType,
            category = recording.category,
            subCategory = recording.subCategory,
            matchPeriod = recording.matchPeriod,
            matchClockStartMillis = recording.matchClockStartMillis,
            matchClockEndMillis = recording.matchClockEndMillis,
            transcriptionId = recording.transcriptionId,
            transcriptText = recording.transcriptText,
            events = recording.events.map { it.toTranscriptionEvent() },
            issues = recording.issues.map { it.toTranscriptionEventIssue() },
        )
    },
)

fun TranscriptionEventInput.toRequestDto() = TranscriptionEventRequestDto(
    text = text,
    startMillis = startMillis,
    endMillis = endMillis,
    eventType = eventType,
    playerOutName = playerOutName,
    playerInName = playerInName,
    playerNames = playerNames,
)

private fun TranscriptionEventDto.toTranscriptionEvent() = TranscriptionEvent(
    eventId = eventId,
    text = text,
    startMillis = startMillis,
    endMillis = endMillis,
    eventType = eventType,
    playerOutName = playerOutName,
    playerInName = playerInName,
    playerNames = playerNames,
    manuallyEdited = manuallyEdited,
    matchPeriod = matchPeriod,
    matchStartMillis = matchStartMillis,
    matchEndMillis = matchEndMillis,
)

private fun TranscriptionEventIssueDto.toTranscriptionEventIssue() = TranscriptionEventIssue(
    issueId,
    issueType,
    relatedEventId,
    candidateText,
    message,
    excerpt,
    startMillis,
    endMillis,
)
