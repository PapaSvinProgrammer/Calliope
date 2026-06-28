package com.mordva.radio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mordva.feature.home.HomeScreenProvider
import com.mordva.navigation.Router
import com.mordva.navigation.route.HomeRoute
import com.mordva.radio.ui.theme.RadioCalliopeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadioCalliopeTheme {
                ComposeRadioApp()
            }
        }
    }
}

@Composable
fun ComposeRadioApp() {
    val backStack = rememberNavBackStack(Router.startDestination)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            entryProvider = { route ->
                when (route) {
                    is HomeRoute -> NavEntry(route) {
                        HomeScreenProvider()
                    }

                    else -> NavEntry(route) {}
                }
            },
        )
    }
}
