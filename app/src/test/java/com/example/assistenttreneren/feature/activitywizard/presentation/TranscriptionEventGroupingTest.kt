package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEvent
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventType
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionRecording
import org.junit.Assert.assertEquals
import org.junit.Test

class TranscriptionEventGroupingTest {
    @Test
    fun `groups substitutions and sorts them by match time`() {
        val recording = recordingWith(
            event("later-substitution", TranscriptionEventType.SUBSTITUTION, startMillis = 40_000, matchStartMillis = 140_000),
            event("observation", TranscriptionEventType.OBSERVATION, startMillis = 10_000, matchStartMillis = 110_000),
            event("earlier-substitution", TranscriptionEventType.SUBSTITUTION, startMillis = 50_000, matchStartMillis = 120_000),
            event("statistic", TranscriptionEventType.STAT, startMillis = 20_000, matchStartMillis = 100_000),
        )

        assertEquals(
            listOf("earlier-substitution", "later-substitution"),
            recording.eventsFor(TranscriptionEventType.SUBSTITUTION).map { it.eventId },
        )
    }

    private fun recordingWith(vararg events: TranscriptionEvent) = TranscriptionRecording(
        recordingId = "recording-1",
        backendRecordingId = "backend-recording-1",
        filename = "match.m4a",
        mediaType = "Audio",
        category = "Kamp",
        subCategory = "1.omgang",
        matchPeriod = "FIRST_HALF",
        matchClockStartMillis = 0L,
        matchClockEndMillis = 1_000L,
        transcriptionId = "transcription-1",
        transcriptText = "Tekst",
        events = events.toList(),
        issues = emptyList(),
    )

    private fun event(
        id: String,
        type: TranscriptionEventType,
        startMillis: Long,
        matchStartMillis: Long,
    ) = TranscriptionEvent(
        eventId = id,
        text = id,
        startMillis = startMillis,
        endMillis = startMillis + 1_000L,
        eventType = type,
        playerOutName = null,
        playerInName = null,
        playerNames = emptyList(),
        manuallyEdited = false,
        matchPeriod = "FIRST_HALF",
        matchStartMillis = matchStartMillis,
        matchEndMillis = matchStartMillis + 1_000L,
    )
}
