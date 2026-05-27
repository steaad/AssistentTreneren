package com.example.assistenttreneren.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assistenttreneren.R
import com.example.assistenttreneren.core.auth.SessionState
import com.example.assistenttreneren.feature.login.presentation.LoginScreen
import com.example.assistenttreneren.feature.login.presentation.LoginViewModel
import com.example.assistenttreneren.ui.theme.AssistentTrenerenTheme

@Composable
fun AppNavGraph(
    sessionState: SessionState,
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
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
                onLogoutClicked = onLogoutClicked,
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
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            Button(
                onClick = onLogoutClicked,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(text = stringResource(R.string.logout_button))
            }
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
            onLogoutClicked = {},
        )
    }
}
