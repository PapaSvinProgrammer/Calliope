package com.mordva.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.domain.domain.repository.CityRepository
import com.mordva.domain.domain.usecase.LoadRadioStationsUseCase
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenEvent
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class HomeViewModel(
    private val cityRepository: CityRepository,
    private val loadRadioStationsUseCase: LoadRadioStationsUseCase,
) : ViewModel() {
    private val isPagerLoadMore = MutableStateFlow(false)

    private val currentPagerIndex = MutableStateFlow(0)
    private val currentStationState = MutableStateFlow<HomeScreenRadioState>(HomeScreenRadioState.Loading)
    private val recommendationStationsState = MutableStateFlow<List<HomeScreenRadioState>>(emptyList())
    private val isPlayRadioState = MutableStateFlow(false)

    private val _uiEvent = Channel<HomeScreenEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getRecommendationStations()
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
    }

    fun onActionHandle(action: HomeScreenAction) = when (action) {
        HomeScreenAction.OnPlayClick -> togglePlayRadio()
        HomeScreenAction.OnSearchClick -> TODO()
        is HomeScreenAction.OnPagerItemClick -> handleSelectedPagerItem(action.page)
        is HomeScreenAction.OnPagerEnded -> loadMoreRadioStation()
    }

    private fun handleSelectedPagerItem(selectedIndex: Int) {
        currentPagerIndex.value = selectedIndex
        sendEvent(HomeScreenEvent.MovePager(selectedIndex))
        updateCurrentRadioStation()
    }

    private fun updateCurrentRadioStation() {
        val stationState = recommendationStationsState.value.getOrNull(currentPagerIndex.value)

        if (stationState is HomeScreenRadioState.Success) {
            currentStationState.value = stationState
        } else {
            sendEvent(HomeScreenEvent.ShowSelectStationErrorMessage)
        }
    }

    private fun togglePlayRadio() {
        isPlayRadioState.value = !isPlayRadioState.value
    }

    private fun sendEvent(event: HomeScreenEvent) = viewModelScope.launch {
        Log.d(TAG, "sendEvent(): event = $event")
        _uiEvent.send(event)
    }

    private fun getRecommendationStations() = viewModelScope.launch {
        Log.d(TAG, "getRecommendationStations()")

        loadRadioStationsUseCase.execute(DEFAULT_PAGER_SIZE).onSuccess { stations ->
            recommendationStationsState.value = stations.map {
                HomeScreenRadioState.Success(station = it)
            }
        }
    }

    private fun loadMoreRadioStation() = viewModelScope.launch {
        if (isPagerLoadMore.value) return@launch
        isPagerLoadMore.value = true

        Log.d(TAG, "loadRadioStation()")

        recommendationStationsState.value += List(DEFAULT_LOADING_PAGER_SIZE) {
            HomeScreenRadioState.Loading
        }

        loadRadioStationsUseCase.execute(DEFAULT_PAGER_SIZE).onSuccess { stations ->
            val contentItems = stations.map { HomeScreenRadioState.Success(station = it) }

            recommendationStationsState.value = recommendationStationsState
                .value
                .dropLast(DEFAULT_LOADING_PAGER_SIZE) + contentItems

            isPagerLoadMore.value = false
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val DEFAULT_PAGER_SIZE = 10
        const val DEFAULT_LOADING_PAGER_SIZE = 1
    }
}