package com.mordva.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.usecase.SearchRadioStationsUseCase
import com.mordva.feature.search.presentation.state.SearchAction
import com.mordva.feature.search.presentation.state.SearchContentState
import com.mordva.feature.search.presentation.state.SearchUiState
import com.mordva.feature.search.presentation.utils.toAudioItem
import com.mordva.player.api.PlaybackManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class SearchViewModel(
    private val searchRadioStationsUseCase: SearchRadioStationsUseCase,
    private val playbackManager: PlaybackManager,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val content = MutableStateFlow<SearchContentState>(SearchContentState.Initial)

    val uiState = combine(
        query,
        content,
        playbackManager.state,
    ) { query, content, playbackState ->
        SearchUiState(
            query = query,
            content = content,
            currentTrackId = playbackState.currentTrackId?.toIntOrNull(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
        initialValue = SearchUiState(),
    )

    private var loadMoreJob: Job? = null
    private var hasMoreStations = true

    init {
        observeQuery()
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnSearchTextChanged -> updateQuery(action.text)
            is SearchAction.OnStationClick -> playbackManager.play(action.station.toAudioItem())
            SearchAction.OnListEnded -> loadMore()
            SearchAction.OnRetryClick -> search(query.value.trim())
        }
    }

    private fun updateQuery(value: String) {
        query.value = value
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() = viewModelScope.launch {
        query
            .debounce(SEARCH_DEBOUNCE)
            .map(String::trim)
            .distinctUntilChanged()
            .collectLatest { value ->
                if (value.length < MIN_QUERY_LENGTH) {
                    hasMoreStations = true
                    content.value = SearchContentState.Initial
                } else {
                    search(value)
                }
            }
    }

    private fun search(value: String) = viewModelScope.launch {
        hasMoreStations = true
        content.value = SearchContentState.Loading

        searchRadioStationsUseCase
            .execute(value, PAGE_SIZE)
            .onSuccess { stations ->
                hasMoreStations = stations.size == PAGE_SIZE
                content.value = if (stations.isEmpty()) {
                    SearchContentState.Empty
                } else {
                    SearchContentState.Success(stations)
                }
            }
            .onFailure {
                content.value = SearchContentState.Error
            }
    }

    private fun loadMore() {
        val value = query.value.trim()

        val current = content.value as? SearchContentState.Success ?: return

        if (!hasMoreStations || current.isLoadingMore || loadMoreJob?.isActive == true) return

        loadMoreJob = viewModelScope.launch {
            content.value = current.copy(isLoadingMore = true)

            searchRadioStationsUseCase.execute(value, PAGE_SIZE)
                .onSuccess { stations ->
                    hasMoreStations = stations.size == PAGE_SIZE
                    appendStations(stations)
                }
                .onFailure {
                    content.value = current.copy(isLoadingMore = false)
                }
        }
    }

    private fun appendStations(stations: List<RadioStation>) {
        val current = content.value as? SearchContentState.Success ?: return
        content.value = current.copy(
            stations = (current.stations + stations).distinctBy(RadioStation::id),
            isLoadingMore = false,
        )
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val PAGE_SIZE = 20
        val SEARCH_DEBOUNCE = 300.milliseconds
        val STOP_TIMEOUT = 5_000.milliseconds
    }
}
