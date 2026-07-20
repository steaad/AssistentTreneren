package com.example.assistenttreneren.feature.recording.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingSubCategoryTest {
    @Test
    fun `forActivityCategory includes evaluation for match`() {
        val subCategories = RecordingSubCategory.forActivityCategory("Kamp")

        assertEquals(
            listOf("1.omgang", "Pause", "2.omgang", "Evaluering"),
            subCategories.map { it.displayName },
        )
    }

    @Test
    fun `forActivityCategory includes evaluation for training`() {
        val subCategories = RecordingSubCategory.forActivityCategory("Trening")

        assertEquals(
            listOf("Spill", "Øvelse", "Evaluering"),
            subCategories.map { it.displayName },
        )
    }
}
