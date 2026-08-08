package com.mordva.feature.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.domain.domain.model.RadioStation
import com.mordva.feature.search.presentation.component.StationListItem
import com.mordva.feature.search.presentation.state.SearchAction
import com.mordva.feature.search.presentation.state.SearchContentState
import com.mordva.feature.search.presentation.state.SearchUiState
import com.mordva.system_ui.Resources
import org.koin.androidx.compose.koinViewModel
import rememberIsNearEnd

@Composable
internal fun SearchScreen(
    query: String,
    onStationClick: (Int) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val isNearEnd = rememberIsNearEnd(lazyListState)

    LaunchedEffect(query) {
        viewModel.onAction(SearchAction.OnSearchTextChanged(query))
    }

    LaunchedEffect(isNearEnd) {
        if (isNearEnd) viewModel.onAction(SearchAction.OnListEnded)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
    ) {
        SearchContent(
            uiState = uiState,
            lazyListState = lazyListState,
            onStationClick = { station ->
                viewModel.onAction(SearchAction.OnStationClick(station))
                onStationClick(station.id)
            },
            onRetryClick = { viewModel.onAction(SearchAction.OnRetryClick) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    lazyListState: LazyListState,
    onStationClick: (RadioStation) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val content = uiState.content) {
            SearchContentState.Initial -> MessageContent(
                text = stringResource(R.string.search_initial_message),
            )

            SearchContentState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )

            SearchContentState.Empty -> MessageContent(
                text = stringResource(R.string.search_empty_message),
            )

            SearchContentState.Error -> Column(
                verticalArrangement = Arrangement.spacedBy(Resources.Dimens.DP12),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(stringResource(R.string.search_error_message))
                Button(onClick = onRetryClick) {
                    Text(stringResource(R.string.retry))
                }
            }

            is SearchContentState.Success -> LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = content.stations,
                    key = { it.id },
                ) { station ->
                    StationListItem(
                        station = station,
                        isPlaying = station.id == uiState.currentTrackId,
                        onClick = { onStationClick(station) },
                    )
                }

                if (content.isLoadingMore) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(Resources.Dimens.DP16),
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = text)
    }
}
