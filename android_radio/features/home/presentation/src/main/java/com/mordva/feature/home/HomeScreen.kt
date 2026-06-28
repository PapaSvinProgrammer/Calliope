package com.mordva.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.feature.home.design.ProgressBarControls
import com.mordva.feature.home.design.topbar.HomeTopBar
import com.mordva.feature.home.state.HomeScreenState
import com.mordva.system_ui.R
import com.mordva.system_ui.Resources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(HomeScreenState())

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = {},
                state = uiState.cityState,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            IconButton(onClick = { viewModel.togglePlayRadio() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_play_circle),
                    contentDescription = null,
                )
            }

            ProgressBarControls(
                state = uiState.radioState,
                isExpanded = uiState.isPlayRadio,
                onPauseClick = { viewModel.togglePlayRadio() },
                modifier = Modifier.padding(horizontal = Resources.Dimens.DP16)
            )
        }
    }
}
