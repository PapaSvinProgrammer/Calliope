package com.mordva.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.domain.domain.usecase.LoadRadioStationsUseCase
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenEvent
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    private val loadRadioStationsUseCase: LoadRadioStationsUseCase,
) : ViewModel() {
    private val currentPagerIndex = MutableStateFlow(0)
    private val currentStationState = MutableStateFlow<HomeScreenRadioState>(HomeScreenRadioState.Loading)
    private val recommendationStationsState = MutableStateFlow<List<HomeScreenRadioState>>(emptyList())
    private val isPlayRadioState = MutableStateFlow(false)

    private val _uiEvent = Channel<HomeScreenEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var loadMoreJob: Job? = null

    init {
        getInitialRadioStations()
    }

    val uiState: Flow<HomeScreenState> = combine(
        isPlayRadioState,
        recommendationStationsState,
        currentStationState,
    ) { isPlayRadio, recommendationStations, currentStation ->
        HomeScreenState(
            radioState = currentStation,
            recommendationStations = recommendationStations,
            isPlayRadio = isPlayRadio,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenState()
    )

    fun onActionHandle(action: HomeScreenAction) = when (action) {
        HomeScreenAction.OnPlayClick -> togglePlayRadio()
        HomeScreenAction.OnSearchClick -> TODO()
        is HomeScreenAction.OnPagerItemClick -> handleSelectedPagerItem(action.page)
        is HomeScreenAction.OnPagerEnded -> loadMoreRadioStation()
    }

    private fun handleSelectedPagerItem(selectedIndex: Int) {
        currentPagerIndex.value = selectedIndex
        sendEvent(HomeScreenEvent.MovePager(selectedIndex))
        updateCurrentRadioStation(selectedIndex)
        updateSelectedStation(selectedIndex)
    }

    private fun updateCurrentRadioStation(selectedIndex: Int) {
        val stationState = recommendationStationsState.value.getOrNull(selectedIndex)

        if (stationState is HomeScreenRadioState.Success) {
            currentStationState.update { stationState }
        } else {
            sendEvent(HomeScreenEvent.ShowSelectStationErrorMessage)
        }
    }

    private fun updateSelectedStation(selectedIndex: Int) {
        val newList = recommendationStationsState.value.mapIndexed { index, item ->
            if (item is HomeScreenRadioState.Success) {
                item.copy(
                    isSelected = index == selectedIndex
                )
            } else {
                item
            }
        }

        recommendationStationsState.update { newList }
    }

    private fun togglePlayRadio() {
        isPlayRadioState.update { !it }
    }

    private fun sendEvent(event: HomeScreenEvent) = viewModelScope.launch {
        Log.d(TAG, "sendEvent(): event = $event")
        _uiEvent.send(event)
    }

    private fun getInitialRadioStations() = viewModelScope.launch {
        Log.d(TAG, "getRecommendationStations()")

        loadRadioStationsUseCase.execute(DEFAULT_PAGER_SIZE).onSuccess { stations ->
            recommendationStationsState.update {
                stations.map { HomeScreenRadioState.Success(station = it) }
            }
        }
    }

    private fun loadMoreRadioStation() {
        if (loadMoreJob?.isActive == true) return

        Log.d(TAG, "loadRadioStation()")
        recommendationStationsState.update { it + HomeScreenRadioState.Loading }

        loadMoreJob = viewModelScope.launch {
            loadRadioStationsUseCase.execute(DEFAULT_PAGER_SIZE).onSuccess { stations ->
                val newItems = stations.map { HomeScreenRadioState.Success(station = it) }

                recommendationStationsState.update { current ->
                    current.dropLast(DEFAULT_LOADING_PAGER_SIZE) + newItems
                }
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val DEFAULT_PAGER_SIZE = 10
        const val DEFAULT_LOADING_PAGER_SIZE = 1
    }
}