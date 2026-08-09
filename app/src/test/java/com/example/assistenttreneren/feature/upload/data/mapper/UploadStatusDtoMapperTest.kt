package com.example.assistenttreneren.feature.upload.data.mapper

import com.example.assistenttreneren.feature.upload.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadStatusDtoMapperTest {
    @Test
    fun `maps every supported backend upload status`() {
        UploadStatus.entries.forEach { expectedStatus ->
            assertEquals(expectedStatus, expectedStatus.name.toUploadStatusFromDto())
        }
    }

    @Test
    fun `maps an unknown backend upload status to failed`() {
        assertEquals(UploadStatus.Failed, "UnknownStatus".toUploadStatusFromDto())
    }
}
