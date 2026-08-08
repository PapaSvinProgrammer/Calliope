package com.mordva.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.filter.presentation.R
import com.mordva.presentation.component.EmptyContent
import com.mordva.presentation.component.FilterSearchBar
import com.mordva.presentation.component.LoadingContent
import com.mordva.presentation.component.LocationSuccessContent
import com.mordva.presentation.state.FilterAction
import com.mordva.presentation.state.FilterAction.OnListEnded
import com.mordva.presentation.state.FilterAction.OnSearchTextChanged
import com.mordva.presentation.state.FilterEvent
import com.mordva.presentation.state.FilterUiState
import com.mordva.presentation.state.LocationCityState
import com.mordva.system_ui.CollectWithLifecycle
import com.mordva.system_ui.Resources
import com.mordva.system_ui.composition_local.LocalSnackbarHostState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import rememberIsNearEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterBottomSheet(
    viewModel: FilterViewModel = koinViewModel<FilterViewModel>(),
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(FilterUiState())
    val snackbarHostState = LocalSnackbarHostState.current
    val errorMessageLoadMoreCity = stringResource(R.string.error_message_load_more_city)
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val lazyListState = rememberLazyListState()
    val isNearEnd = rememberIsNearEnd(lazyListState)

    LaunchedEffect(isNearEnd) {
        if (isNearEnd) {
            viewModel.onActionHandle(OnListEnded)
        }
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { viewModel.onActionHandle(OnSearchTextChanged(it)) }
    }

    CollectWithLifecycle(viewModel.uiEvent) {
        if (it == FilterEvent.SendLoadMoreError) {
            snackbarHostState.showSnackbar(errorMessageLoadMoreCity)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxHeight(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterSearchBar(
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onVoiceInputClick = {},
            )

            Spacer(modifier = Modifier.height(Resources.Dimens.DP12))

            Box(modifier = Modifier.weight(1f)) {
                FilterContent(
                    uiState = uiState,
                    lazyListState = lazyListState,
                    onAction = viewModel::onActionHandle,
                )
            }
        }
    }
}

@Composable
private fun FilterContent(
    uiState: FilterUiState,
    lazyListState: LazyListState,
    onAction: (FilterAction) -> Unit,
) {
    when (val state = uiState.cityListState) {
        LocationCityState.Loading -> LoadingContent()
        is LocationCityState.Success -> LocationSuccessContent(
            cityListState = state,
            onItemClick = { onAction(FilterAction.OnLocationClick(it)) },
            lazyListState = lazyListState,
            selectedCityIds = uiState.selectedCities.mapTo(mutableSetOf()) { it.id },
        )

        LocationCityState.Error -> EmptyContent("Could not load cities")
        LocationCityState.Init -> Unit
    }
}
