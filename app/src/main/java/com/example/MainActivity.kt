package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.security.KeyManager
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FruitArenaScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    LOGIN,
    DASHBOARD,
    PRACTICE_ARENA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val isActivatedInitially = remember { KeyManager.isActivated(this) }
                    var currentScreen by remember {
                        mutableStateOf(if (isActivatedInitially) Screen.DASHBOARD else Screen.LOGIN)
                    }

                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    currentScreen = Screen.DASHBOARD
                                }
                            )
                        }
                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                onOpenPracticeArena = {
                                    currentScreen = Screen.PRACTICE_ARENA
                                },
                                onLogout = {
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }
                        Screen.PRACTICE_ARENA -> {
                            FruitArenaScreen(
                                onBack = {
                                    currentScreen = Screen.DASHBOARD
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

