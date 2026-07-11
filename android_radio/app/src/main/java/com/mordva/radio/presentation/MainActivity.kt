package com.mordva.radio.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mordva.control_bar.ControlBottomBar
import com.mordva.feature.home.HomeScreenProvider
import com.mordva.navigation.Router
import com.mordva.navigation.route.HomeRoute
import com.mordva.radio.domain.MainViewModel
import com.mordva.radio.domain.action
import com.mordva.radio.presentation.theme.AppTheme
import com.mordva.system_ui.Resources
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme {
                ComposeRadioApp()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged()")
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

@Composable
private fun ComposeRadioApp(
    viewModel: MainViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(MainUiState())
    val backStack = rememberNavBackStack(Router.startDestination)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            ControlBottomBar(
                searchExpanded = uiState.searchExpanded,
                searchText = uiState.searchText,
                selectedItem = uiState.selectedItem,
                action = { viewModel.action(it) },
                modifier = Modifier.padding(Resources.Dimens.DP10),
            )
        }
    ) { innerPadding ->
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
