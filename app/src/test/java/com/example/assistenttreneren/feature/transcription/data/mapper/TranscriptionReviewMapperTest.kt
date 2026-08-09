package com.example.assistenttreneren.feature.transcription.data.mapper

import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionEventIssueDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionRecordingDto
import com.example.assistenttreneren.feature.transcription.data.dto.TranscriptionReviewDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranscriptionReviewMapperTest {
    @Test
    fun `maps recording presentation data events and issues from review response`() {
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
                            manuallyEdited = true,
                        ),
                    ),
                    issues = listOf(
                        TranscriptionEventIssueDto(
                            issueId = "issue-1",
                            issueType = "UnknownPlayer",
                            candidateText = "Ola",
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
        assertEquals("Press", recording.events.single().text)
        assertTrue(recording.events.single().manuallyEdited)
        assertEquals("Ola", recording.issues.single().candidateText)
        assertEquals(4_000L, recording.issues.single().startMillis)
    }
}
