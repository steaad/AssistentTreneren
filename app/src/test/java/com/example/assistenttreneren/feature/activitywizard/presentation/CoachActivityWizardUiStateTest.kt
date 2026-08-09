package com.example.assistenttreneren.feature.activitywizard.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachActivityWizardUiStateTest {
    @Test
    fun `create activity can continue when title and category are selected`() {
        val state = CoachActivityWizardUiState(
            activityInputMode = CoachActivityInputMode.CreateNew,
            title = "Onsdagstrening",
            activityCategory = "Trening",
        )

        assertTrue(state.isCreateActivityFormVisible)
        assertTrue(state.canContinueFromActivityType)
    }

    @Test
    fun `existing activity can continue only when selected activity is in the loaded list`() {
        val selectedActivity = ExistingCoachActivityUiModel(
            activityId = "activity-1",
            activityCategory = "Kamp",
            title = "Hjemmekamp",
        )
        val state = CoachActivityWizardUiState(
            activityInputMode = CoachActivityInputMode.Existing,
            existingActivities = listOf(selectedActivity),
            selectedExistingActivityId = selectedActivity.activityId,
            selectedActivityId = selectedActivity.activityId,
        )

        assertTrue(state.isExistingActivityFormVisible)
        assertTrue(state.canContinueFromActivityType)
    }

    @Test
    fun `activity step cannot continue while an activity operation is in progress`() {
        val state = CoachActivityWizardUiState(
            activityInputMode = CoachActivityInputMode.CreateNew,
            title = "Onsdagstrening",
            activityCategory = "Trening",
            isCreatingActivity = true,
        )

        assertFalse(state.canContinueFromActivityType)
    }
}
