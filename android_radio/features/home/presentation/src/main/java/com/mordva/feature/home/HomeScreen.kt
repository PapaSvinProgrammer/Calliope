package com.mordva.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.feature.home.component.BodyContent
import com.mordva.feature.home.component.pager.RadioCoverPager
import com.mordva.feature.home.component.topbar.HomeTopBar
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenEvent
import com.mordva.feature.home.state.HomeScreenState
import com.mordva.system_ui.CollectWithLifecycle
import com.mordva.system_ui.composition_local.LocalSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(HomeScreenState())
    val pagerState = rememberPagerState { uiState.recommendationStations.size }

    val snackbarHostState = LocalSnackbarHostState.current

    val errorMessageSelectRadioStation = stringResource(R.string.error_message_selected_radio_station)

    CollectWithLifecycle(viewModel.uiEvent) { event ->
        when (event) {
            is HomeScreenEvent.MovePager -> pagerState.animateScrollToPage(event.position)

            HomeScreenEvent.ShowSelectStationErrorMessage -> {
                snackbarHostState.showSnackbar(errorMessageSelectRadioStation)
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page == pagerState.pageCount - 2) {
                viewModel.onActionHandle(HomeScreenAction.OnPagerEnded)
            }
        }
    }


    Scaffold(
        topBar = { HomeTopBar(uiState.cityState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            BodyContent(
                uiState = uiState,
                onAction = { viewModel.onActionHandle(it) },
                modifier = Modifier.weight(1f),
            )

            RadioCoverPager(
                items = uiState.recommendationStations,
                pagerState = pagerState,
                onClickPagerItem = { viewModel.onActionHandle(HomeScreenAction.OnPagerItemClick(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}
