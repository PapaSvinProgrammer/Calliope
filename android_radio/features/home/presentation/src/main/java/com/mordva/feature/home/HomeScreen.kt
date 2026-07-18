package com.mordva.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.feature.home.component.BodyContent
import com.mordva.feature.home.component.pager.RadioCoverPager
import com.mordva.feature.home.component.topbar.HomeTopBar
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenEvent
import com.mordva.feature.home.state.HomeScreenState
import com.mordva.system_ui.CollectWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(HomeScreenState())
    val pagerState = rememberPagerState { uiState.recommendationStations.size }

    CollectWithLifecycle(viewModel.uiEvent) { event ->
        when (event) {
            is HomeScreenEvent.MovePager -> pagerState.animateScrollToPage(event.position)
        }
    }

    Scaffold(
        topBar = { HomeTopBar(uiState.cityState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            BodyContent(
                uiState = uiState,
                onAction = { viewModel.onActionHandle(it) },
                modifier = Modifier
                    .weight(1f)
                    .zIndex(1f),
            )

            RadioCoverPager(
                items = uiState.recommendationStations,
                pagerState = pagerState,
                onClickPagerItem = { viewModel.onActionHandle(HomeScreenAction.OnPagerItemClick(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(2f),
            )
        }
    }
}
