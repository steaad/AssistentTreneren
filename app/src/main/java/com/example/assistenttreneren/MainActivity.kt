package com.example.assistenttreneren

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.assistenttreneren.feature.login.presentation.LoginViewModel
import com.example.assistenttreneren.navigation.AppNavGraph
import com.example.assistenttreneren.navigation.AppSessionViewModel
import com.example.assistenttreneren.ui.theme.AssistentTrenerenTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appSessionViewModel: AppSessionViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionState by appSessionViewModel.sessionState.collectAsState()

            AssistentTrenerenTheme {
                AppNavGraph(
                    sessionState = sessionState,
                    loginViewModel = loginViewModel,
                    onLoginSuccess = appSessionViewModel::onLoginSucceeded,
                    onLogoutClicked = appSessionViewModel::logout,
                )
            }
        }
    }
}
