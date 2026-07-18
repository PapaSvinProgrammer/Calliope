package com.mordva.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.domain.domain.repository.CityRepository
import com.mordva.domain.domain.repository.RadioStationRepository
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
    private val radioStationRepository: RadioStationRepository,
) : ViewModel() {
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
    ) { isPlayRadio, recommendationStations ->
        HomeScreenState(
            recommendationStations = recommendationStations,
            isPlayRadio = isPlayRadio,
        )
    }

    fun onActionHandle(action: HomeScreenAction) = when (action) {
        HomeScreenAction.OnPlayClick -> togglePlayRadio()
        HomeScreenAction.OnSearchClick -> TODO()
        is HomeScreenAction.OnPagerItemClick -> handleSelectedPagerItem(action.page)
    }

    private fun handleSelectedPagerItem(selectedIndex: Int) {
        sendEvent(HomeScreenEvent.MovePager(selectedIndex))
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

        radioStationRepository.getAll().onSuccess { stations ->
            recommendationStationsState.value = stations.map {
                HomeScreenRadioState.Success(station = it)
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}