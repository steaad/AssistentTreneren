package com.example.assistenttreneren.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.assistenttreneren.R
import com.example.assistenttreneren.core.auth.SessionState
import com.example.assistenttreneren.feature.activitywizard.presentation.CoachActivityWizardViewModel
import com.example.assistenttreneren.feature.activitywizard.presentation.steps.ActivityTypeStepScreen
import com.example.assistenttreneren.feature.activitywizard.presentation.steps.AudioRecordingStepScreen
import com.example.assistenttreneren.feature.activitywizard.presentation.steps.SummaryStepScreen
import com.example.assistenttreneren.feature.activitywizard.presentation.steps.UploadStepScreen
import com.example.assistenttreneren.feature.login.presentation.LoginScreen
import com.example.assistenttreneren.feature.login.presentation.LoginViewModel
import com.example.assistenttreneren.ui.theme.AssistentTrenerenTheme

@Composable
fun AppNavGraph(
    sessionState: SessionState,
    loginViewModel: LoginViewModel,
    onLoginSuccess: (isBackendBypass: Boolean) -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val sessionExpiredMessage = stringResource(R.string.session_expired_message)
    val targetRoute = remember(sessionState) {
        sessionState.toRoute()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Loading.route,
        modifier = modifier,
    ) {
        composable(Routes.Loading.route) {
            LoadingScreen()
        }

        composable(Routes.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = onLoginSuccess,
                sessionMessage = if (sessionState == SessionState.SessionExpired) {
                    sessionExpiredMessage
                } else {
                    null
                },
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onCoachActivityClicked = {
                    navController.navigate(Routes.CoachActivity.route)
                },
                onAnalysisClicked = {
                    navController.navigate(Routes.Analysis.route)
                },
                onHistoryClicked = {
                    navController.navigate(Routes.History.route)
                },
                onLogoutClicked = onLogoutClicked,
            )
        }

        navigation(
            route = Routes.CoachActivity.route,
            startDestination = Routes.CoachActivityType.route,
        ) {
            composable(Routes.CoachActivityType.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CoachActivity.route)
                }
                val viewModel: CoachActivityWizardViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsState()

                ActivityTypeStepScreen(
                    uiState = uiState,
                    onStepOpened = viewModel::onStepOpened,
                    onCreateNewActivityClicked = viewModel::onCreateNewActivityClicked,
                    onSelectExistingActivityClicked = viewModel::onSelectExistingActivityClicked,
                    onTitleChanged = viewModel::onTitleChanged,
                    onActivityCategorySelected = viewModel::onActivityCategorySelected,
                    onExistingActivitySelected = viewModel::onExistingActivitySelected,
                    onMatchRosterChanged = viewModel::onMatchRosterChanged,
                    onMatchRosterSuggestionSelected = viewModel::onMatchRosterSuggestionSelected,
                    onMatchHalfDurationChanged = viewModel::onMatchHalfDurationChanged,
                    onRetryLoadExistingActivitiesClicked = viewModel::onRetryLoadExistingActivitiesClicked,
                    onNavigateBack = navController::popBackStack,
                    onNavigateNext = {
                        viewModel.onContinueFromActivityType {
                            navController.navigate(Routes.CoachActivityAudioRecording.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable(Routes.CoachActivityAudioRecording.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CoachActivity.route)
                }
                val viewModel: CoachActivityWizardViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsState()

                AudioRecordingStepScreen(
                    uiState = uiState,
                    onStepOpened = viewModel::onStepOpened,
                    onNavigateBack = navController::popBackStack,
                    onNavigateNext = {
                        navController.navigate(Routes.CoachActivityUpload.route)
                    },
                )
            }

            composable(Routes.CoachActivityUpload.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CoachActivity.route)
                }
                val viewModel: CoachActivityWizardViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsState()

                UploadStepScreen(
                    uiState = uiState,
                    onStepOpened = viewModel::onStepOpened,
                    onNavigateBack = navController::popBackStack,
                    onNavigateNext = {
                        navController.navigate(Routes.CoachActivitySummary.route)
                    },
                )
            }

            composable(Routes.CoachActivitySummary.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.CoachActivity.route)
                }
                val viewModel: CoachActivityWizardViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsState()

                SummaryStepScreen(
                    uiState = uiState,
                    onStepOpened = viewModel::onStepOpened,
                    onNavigateBack = navController::popBackStack,
                    onFinish = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.CoachActivity.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable(Routes.Analysis.route) {
            SimpleDestinationScreen(
                title = stringResource(R.string.analysis_title),
                onNavigateBack = navController::popBackStack,
            )
        }

        composable(Routes.History.route) {
            SimpleDestinationScreen(
                title = stringResource(R.string.history_title),
                onNavigateBack = navController::popBackStack,
            )
        }
    }

    LaunchedEffect(targetRoute) {
        if (navController.currentBackStackEntry?.destination?.route == targetRoute) {
            return@LaunchedEffect
        }

        navController.navigate(targetRoute) {
            val currentDestinationId = navController.currentDestination?.id
                ?: navController.graph.findStartDestination().id

            popUpTo(currentDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}

private fun SessionState.toRoute(): String =
    when (this) {
        SessionState.Loading -> Routes.Loading.route
        SessionState.Authenticated -> Routes.Home.route
        SessionState.Unauthenticated,
        SessionState.SessionExpired,
        -> Routes.Login.route
    }

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeScreen(
    onCoachActivityClicked: () -> Unit,
    onAnalysisClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(88.dp))

            HomeNavigationTile(
                title = stringResource(R.string.coach_activity_title),
                onClick = onCoachActivityClicked,
            )

            HomeNavigationTile(
                title = stringResource(R.string.analysis_title),
                onClick = onAnalysisClicked,
            )

            HomeNavigationTile(
                title = stringResource(R.string.history_title),
                onClick = onHistoryClicked,
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogoutClicked,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.logout_button))
            }
        }
    }
}

@Composable
private fun HomeNavigationTile(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .height(88.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SimpleDestinationScreen(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TextButton(
                onClick = onNavigateBack,
            ) {
                Text(text = stringResource(R.string.navigate_back_button))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(R.string.screen_under_development),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    AssistentTrenerenTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AssistentTrenerenTheme {
        HomeScreen(
            onCoachActivityClicked = {},
            onAnalysisClicked = {},
            onHistoryClicked = {},
            onLogoutClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleDestinationScreenPreview() {
    AssistentTrenerenTheme {
        SimpleDestinationScreen(
            title = stringResource(R.string.coach_activity_title),
            onNavigateBack = {},
        )
    }
}
