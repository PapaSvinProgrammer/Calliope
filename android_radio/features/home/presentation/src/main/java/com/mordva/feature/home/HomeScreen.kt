package com.mordva.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.feature.home.component.BodyContent
import com.mordva.feature.home.component.BottomContent
import com.mordva.feature.home.component.pager.RadioCoverPager
import com.mordva.feature.home.component.topbar.HomeTopBar
import com.mordva.feature.home.state.HomeScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(HomeScreenState())

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
                modifier = Modifier.weight(1f),
            )

            RadioCoverPager(
                items = uiState.recommendationRadios,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )

//            BottomContent(
//                radios = uiState.recommendationRadios,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            )
        }
    }
}
