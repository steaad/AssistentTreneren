package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.feature.activitywizard.domain.model.LearningCatalogItem
import com.example.assistenttreneren.feature.activitywizard.domain.model.TeamFunction
import com.example.assistenttreneren.feature.activitywizard.domain.model.TrainingLearningConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachActivityWizardUiStateTest {
    @Test
    fun `training activity requires a complete learning configuration`() {
        val state = CoachActivityWizardUiState(
            activityInputMode = CoachActivityInputMode.CreateNew,
            title = "Onsdagstrening",
            activityCategory = "Trening",
        )

        assertTrue(state.isCreateActivityFormVisible)
        assertFalse(state.canContinueFromActivityType)

        val configuredState = state.copy(
            trainingLearningConfig = TrainingLearningConfig(
                teamFunction = TeamFunction.ATTACK,
                theme = LearningCatalogItem("theme", "Angrep", null, null, TeamFunction.ATTACK),
                objectives = listOf(LearningCatalogItem("objective", "Avslutning", null, "theme", null)),
            ),
        )

        assertTrue(configuredState.canContinueFromActivityType)
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
