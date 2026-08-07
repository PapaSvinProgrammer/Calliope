package com.mordva.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.FilterPreferencesRepository
import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.usecase.LoadRadioStationsUseCase
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenEvent
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import com.mordva.feature.home.utils.toUiState
import com.mordva.player.api.PlaybackManager
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
    filterPreferencesRepository: FilterPreferencesRepository,
    private val loadRadioStationsUseCase: LoadRadioStationsUseCase,
    private val playbackManager: PlaybackManager,
) : ViewModel() {
    private val currentStationState =
        MutableStateFlow<HomeScreenRadioState>(HomeScreenRadioState.Loading)
    private val recommendationStationsState =
        MutableStateFlow<List<HomeScreenRadioState>>(emptyList())
    private val isPlayRadioState = MutableStateFlow(false)

    private val _uiEvent = Channel<HomeScreenEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var loadMoreJob: Job? = null

    init {
        getInitialRadioStations()
        observePlaybackState()
    }

    val uiState: Flow<HomeScreenState> = combine(
        isPlayRadioState,
        recommendationStationsState,
        currentStationState,
        filterPreferencesRepository.get(),
    ) { isPlayRadio, recommendationStations, currentStation, filters ->
        HomeScreenState(
            radioState = currentStation,
            recommendationStations = recommendationStations,
            isPlayRadio = isPlayRadio,
            cityState = (filters.cities.firstOrNull() ?: CityData()).toUiState(),
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
        sendEvent(HomeScreenEvent.MovePager(selectedIndex))
        updateCurrentRadioStation(selectedIndex)
    }

    private fun updateCurrentRadioStation(selectedIndex: Int) {
        val stationState = recommendationStationsState.value.getOrNull(selectedIndex)

        if (stationState is HomeScreenRadioState.Success) {
            currentStationState.update { stationState }
        } else {
            sendEvent(HomeScreenEvent.ShowSelectStationErrorMessage)
        }
    }

    private fun togglePlayRadio() {
        playbackManager.togglePlayPause()
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
        showLoadMoreLoading()

        loadMoreJob = viewModelScope.launch {
            loadRadioStationsUseCase
                .execute(DEFAULT_PAGER_SIZE)
                .onSuccess { stations ->
                    appendLoadedStations(stations)
                }
                .onFailure {
                    hideLoadMoreLoading()
                    sendEvent(HomeScreenEvent.ShowLoadMoreErrorMessage)
                }
        }
    }

    private fun showLoadMoreLoading() {
        recommendationStationsState.update { it + HomeScreenRadioState.Loading }
    }

    private fun hideLoadMoreLoading() {
        recommendationStationsState.update { it.dropLast(DEFAULT_LOADING_PAGER_SIZE) }
    }

    private fun appendLoadedStations(stations: List<RadioStation>) {
        val newItems = stations.map { HomeScreenRadioState.Success(station = it) }

        recommendationStationsState.update { current ->
            current.dropLast(DEFAULT_LOADING_PAGER_SIZE) + newItems
        }
    }

    private fun observePlaybackState() = viewModelScope.launch {
        playbackManager.state.collect {
            Log.d(TAG, "observePlaybackState = $it")
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val DEFAULT_PAGER_SIZE = 10
        const val DEFAULT_LOADING_PAGER_SIZE = 1
    }
}