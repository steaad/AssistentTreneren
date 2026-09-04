package com.example.assistenttreneren.feature.activitywizard.presentation

import com.example.assistenttreneren.MainDispatcherRule
import com.example.assistenttreneren.core.auth.AuthTokenRefresher
import com.example.assistenttreneren.core.auth.AuthTokens
import com.example.assistenttreneren.core.auth.JwtDecoder
import com.example.assistenttreneren.core.auth.SessionManager
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.feature.activitywizard.domain.model.CoachActivity
import com.example.assistenttreneren.feature.activitywizard.domain.model.MatchRosterSuggestion
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityRepository
import com.example.assistenttreneren.feature.activitywizard.domain.repository.CoachActivityResult
import com.example.assistenttreneren.feature.activitywizard.domain.repository.LocalCoachActivityRepository
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.CreateCoachActivityUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.GetCoachActivitiesUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.UpdateCoachActivityUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.GetMatchRosterUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.GetMatchRosterSuggestionsUseCase
import com.example.assistenttreneren.feature.activitywizard.domain.usecase.UpdateMatchRosterUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoachActivityWizardViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `creates updates persists and navigates for a valid new activity`() = runTest {
        val repository = FakeCoachActivityRepository()
        val localRepository = FakeLocalCoachActivityRepository()
        val viewModel = viewModel(repository, localRepository)
        var navigateCount = 0

        viewModel.onCreateNewActivityClicked()
        viewModel.onTitleChanged("  Onsdagstrening  ")
        viewModel.onActivityCategorySelected("Trening")
        viewModel.onContinueFromActivityType { navigateCount++ }
        runCurrent()

        assertEquals("created-activity", viewModel.uiState.value.selectedActivityId)
        assertEquals("Onsdagstrening", repository.updateTitle)
        assertEquals("Trening", repository.updateCategory)
        assertEquals("Onsdagstrening", localRepository.savedActivities.single().title)
        assertEquals(1, navigateCount)
    }

    @Test
    fun `updates selected existing activity title and navigates`() = runTest {
        val existing = CoachActivity("activity-1", "Trening", "Opprinnelig", emptyList())
        val repository = FakeCoachActivityRepository(activities = listOf(existing))
        val localRepository = FakeLocalCoachActivityRepository()
        val viewModel = viewModel(repository, localRepository)
        var navigateCount = 0

        viewModel.onSelectExistingActivityClicked()
        runCurrent()
        viewModel.onExistingActivitySelected(existing.activityId)
        viewModel.onTitleChanged("Nytt navn")
        viewModel.onContinueFromActivityType { navigateCount++ }
        runCurrent()

        assertEquals("Nytt navn", repository.updateTitle)
        assertEquals("Nytt navn", localRepository.savedActivities.last().title)
        assertEquals(1, navigateCount)
    }

    @Test
    fun `loads and applies roster suggestions for a new match`() = runTest {
        val suggestion = MatchRosterSuggestion(
            sourceActivityId = "previous-match",
            title = "G14 mot Nordstrand",
            playerNames = listOf("Noah", "Olav"),
        )
        val viewModel = viewModel(
            repository = FakeCoachActivityRepository(matchRosterSuggestions = listOf(suggestion)),
            localRepository = FakeLocalCoachActivityRepository(),
        )

        viewModel.onCreateNewActivityClicked()
        viewModel.onActivityCategorySelected("Kamp")
        runCurrent()
        viewModel.onMatchRosterSuggestionSelected(suggestion.playerNames)

        assertEquals(listOf(suggestion), viewModel.uiState.value.matchRosterSuggestions)
        assertEquals(listOf("Noah", "Olav"), viewModel.uiState.value.matchRoster)
    }

    private fun viewModel(
        repository: FakeCoachActivityRepository,
        localRepository: FakeLocalCoachActivityRepository,
    ) = CoachActivityWizardViewModel(
        GetCoachActivitiesUseCase(repository),
        CreateCoachActivityUseCase(repository),
        UpdateCoachActivityUseCase(repository),
        GetMatchRosterUseCase(repository),
        GetMatchRosterSuggestionsUseCase(repository),
        UpdateMatchRosterUseCase(repository),
        localRepository,
        SessionManager(FakeTokenStorage(), JwtDecoder(), FakeTokenRefresher()),
    )

    private class FakeCoachActivityRepository(
        private val activities: List<CoachActivity> = emptyList(),
        private val matchRosterSuggestions: List<MatchRosterSuggestion> = emptyList(),
    ) : CoachActivityRepository {
        var updateTitle: String? = null
        var updateCategory: String? = null
        override suspend fun createActivity() = CoachActivityResult.Success(CoachActivity("created-activity", null, null, emptyList()))
        override suspend fun updateActivity(activityId: String, activityCategory: String?, title: String?): CoachActivityResult<CoachActivity> {
            updateTitle = title
            updateCategory = activityCategory
            return CoachActivityResult.Success(CoachActivity(activityId, activityCategory ?: "Kamp", title, emptyList()))
        }
        override suspend fun getActivities() = CoachActivityResult.Success(activities)
        override suspend fun getActivity(activityId: String) = CoachActivityResult.Success(activities.first { it.activityId == activityId })
        override suspend fun getMatchRoster(activityId: String) = CoachActivityResult.Success(emptyList<String>())
        override suspend fun getMatchRosterSuggestions() = CoachActivityResult.Success(matchRosterSuggestions)
        override suspend fun updateMatchRoster(activityId: String, playerNames: List<String>) = CoachActivityResult.Success(playerNames)
    }

    private class FakeLocalCoachActivityRepository : LocalCoachActivityRepository {
        val savedActivities = mutableListOf<CoachActivity>()
        override fun observeActivities(): Flow<List<CoachActivity>> = flowOf(emptyList())
        override fun observeActivity(activityId: String): Flow<CoachActivity?> = flowOf(null)
        override suspend fun saveActivity(activity: CoachActivity) { savedActivities += activity }
        override suspend fun saveActivities(activities: List<CoachActivity>) { savedActivities += activities }
    }

    private class FakeTokenStorage : TokenStorage {
        override suspend fun saveTokens(tokens: AuthTokens) = Unit
        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override suspend fun clearTokens() = Unit
    }
    private class FakeTokenRefresher : AuthTokenRefresher { override suspend fun refreshTokens(): AuthTokens? = null }
}
