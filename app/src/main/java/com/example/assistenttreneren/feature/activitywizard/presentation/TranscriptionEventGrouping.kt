package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEvent
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventType
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionRecording

internal fun TranscriptionRecording.eventsFor(type: TranscriptionEventType): List<TranscriptionEvent> =
    events
        .asSequence()
        .filter { it.eventType == type }
        .sortedWith(
            compareBy<TranscriptionEvent> { it.matchStartMillis ?: it.startMillis }
                .thenBy { it.matchEndMillis ?: it.endMillis }
                .thenBy { it.eventId },
        )
        .toList()
