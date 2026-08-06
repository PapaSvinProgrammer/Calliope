package com.mordva.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mordva.location.presentation.R
import com.mordva.presentation.component.SuccessContent
import com.mordva.presentation.state.FilterAction.OnItemClick
import com.mordva.presentation.state.FilterAction.OnListEnded
import com.mordva.presentation.state.FilterAction.OnSearchTextChanged
import com.mordva.presentation.state.LocationCityState
import com.mordva.presentation.state.FilterEvent
import com.mordva.presentation.state.FilterUiState
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
            .collect { text ->
                viewModel.onActionHandle(OnSearchTextChanged(text))
            }
    }

    CollectWithLifecycle(viewModel.uiEvent) {
        when (it) {
            FilterEvent.SendLoadMoreError -> {
                snackbarHostState.showSnackbar(errorMessageLoadMoreCity)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LocationSearchBar(
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onVoiceInputClick = {}
            )

            Spacer(modifier = Modifier.height(Resources.Dimens.DP16))

            when (val state = uiState.cityListState) {
                LocationCityState.Loading -> LoadingContent()
                is LocationCityState.Success -> {
                    SuccessContent(
                        cityListState = state,
                        onItemClick = {
                            viewModel.onActionHandle(OnItemClick(it))
                        },
                        lazyListState = lazyListState,
                        currentCityId = uiState.currentCity?.id
                    )
                }

                LocationCityState.Error -> Unit
                LocationCityState.Init -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        LoadingIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun LocationSearchBar(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onVoiceInputClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        state = searchBarState,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                placeholder = {
                    Text(text = stringResource(R.string.title_search_placeholder))
                },
                onSearch = {},
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onVoiceInputClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_mic),
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        modifier = modifier
            .padding(horizontal = Resources.Dimens.DP16)
            .fillMaxWidth()
    )
}