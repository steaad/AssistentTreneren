package com.example.assistenttreneren.feature.transcription.data.mapper

import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventIssueDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionRecordingDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionReviewDto
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventInput
import com.example.assistenttreneren.feature.transcription.domain.model.TranscriptionEventType
import com.example.assistenttreneren.feature.transcription.domain.model.isValid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptionReviewMapperTest {
    @Test
    fun `maps typed events and issues from review response`() {
        val review = TranscriptionReviewDto(
            activityId = "activity-1",
            recordings = listOf(
                TranscriptionRecordingDto(
                    recordingId = "local-recording-1",
                    backendRecordingId = "backend-recording-1",
                    filename = "trening.m4a",
                    mediaType = "Audio",
                    category = "Trening",
                    subCategory = "Øvelse",
                    transcriptionId = "transcription-1",
                    transcriptText = "Press høyere.",
                    events = listOf(
                        TranscriptionEventDto(
                            eventId = "event-1",
                            text = "Press",
                            startMillis = 1_500,
                            endMillis = 3_000,
                            eventType = TranscriptionEventType.OBSERVATION,
                            manuallyEdited = true,
                        ),
                        TranscriptionEventDto(
                            eventId = "statistic-1",
                            text = "Ballbesittelse 60 %",
                            startMillis = 2_000,
                            endMillis = 2_500,
                            eventType = TranscriptionEventType.STAT,
                        ),
                        TranscriptionEventDto(
                            eventId = "substitution-1",
                            text = "Spillerbytte",
                            startMillis = 5_000,
                            endMillis = 5_500,
                            eventType = TranscriptionEventType.SUBSTITUTION,
                            playerOutName = "Amir",
                            playerInName = "Ola",
                        ),
                        TranscriptionEventDto(
                            eventId = "lineup-1",
                            text = "Startoppstilling",
                            startMillis = 0,
                            endMillis = 1_000,
                            eventType = TranscriptionEventType.STARTING_LINEUP,
                            playerNames = listOf("Max", "Noah", "Fabian"),
                        ),
                    ),
                    issues = listOf(
                        TranscriptionEventIssueDto(
                            issueId = "issue-1",
                            issueType = "UnknownPlayer",
                            relatedEventId = "lineup-1",
                            candidateText = "Ola",
                            message = "Spilleren ble ikke gjenkjent.",
                            excerpt = "Bytte Ola inn Slutt",
                            startMillis = 4_000,
                            endMillis = 5_000,
                        ),
                    ),
                ),
            ),
        ).toTranscriptionReview()

        val recording = review.recordings.single()
        assertEquals("activity-1", review.activityId)
        assertEquals("local-recording-1", recording.recordingId)
        assertEquals("backend-recording-1", recording.backendRecordingId)
        assertEquals("trening.m4a", recording.filename)
        assertEquals("Audio", recording.mediaType)
        assertEquals("Trening", recording.category)
        assertEquals("Øvelse", recording.subCategory)
        assertEquals("Press høyere.", recording.transcriptText)
        assertEquals("Press", recording.events.first { it.eventType == TranscriptionEventType.OBSERVATION }.text)
        assertEquals("Ballbesittelse 60 %", recording.events.first { it.eventType == TranscriptionEventType.STAT }.text)
        val substitution = recording.events.first { it.eventType == TranscriptionEventType.SUBSTITUTION }
        assertEquals("Amir", substitution.playerOutName)
        assertEquals("Ola", substitution.playerInName)
        assertEquals(
            listOf("Max", "Noah", "Fabian"),
            recording.events.first { it.eventType == TranscriptionEventType.STARTING_LINEUP }.playerNames,
        )
        assertTrue(recording.events.first { it.eventType == TranscriptionEventType.OBSERVATION }.manuallyEdited)
        assertEquals("Ola", recording.issues.single().candidateText)
        assertEquals("lineup-1", recording.issues.single().relatedEventId)
        assertEquals("Spilleren ble ikke gjenkjent.", recording.issues.single().message)
        assertEquals("Bytte Ola inn Slutt", recording.issues.single().excerpt)
        assertEquals(4_000L, recording.issues.single().startMillis)
    }

    @Test
    fun `maps manual substitution input to request fields`() {
        val request = TranscriptionEventInput(
            text = "Spillerbytte",
            startMillis = 100,
            endMillis = 250,
            eventType = TranscriptionEventType.SUBSTITUTION,
            playerOutName = "Amir",
            playerInName = "Ola",
        ).toRequestDto()

        assertEquals(TranscriptionEventType.SUBSTITUTION, request.eventType)
        assertEquals("Amir", request.playerOutName)
        assertEquals("Ola", request.playerInName)
    }

    @Test
    fun `rejects manual substitution without both player names`() {
        val input = TranscriptionEventInput(
            text = "Spillerbytte",
            startMillis = 100,
            endMillis = 250,
            eventType = TranscriptionEventType.SUBSTITUTION,
            playerOutName = "Amir",
        )

        assertTrue(!input.isValid())
    }

    @Test
    fun `maps and validates starting lineup player names`() {
        val request = TranscriptionEventInput(
            text = "Max, Noah, Fabian",
            startMillis = 0,
            endMillis = 1_000,
            eventType = TranscriptionEventType.STARTING_LINEUP,
            playerNames = listOf("Max", "Noah", "Fabian"),
        ).toRequestDto()

        assertEquals(listOf("Max", "Noah", "Fabian"), request.playerNames)
        assertTrue(
            !TranscriptionEventInput(
                text = "Startoppstilling",
                startMillis = 0,
                endMillis = 1_000,
                eventType = TranscriptionEventType.STARTING_LINEUP,
            ).isValid(),
        )
    }
}
