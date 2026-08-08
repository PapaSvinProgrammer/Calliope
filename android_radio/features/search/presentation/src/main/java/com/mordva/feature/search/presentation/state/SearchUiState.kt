package com.mordva.feature.search.presentation.state

import com.mordva.domain.domain.model.RadioStation

internal data class SearchUiState(
    val query: String = "",
    val content: SearchContentState = SearchContentState.Initial,
    val currentTrackId: Int? = null,
)

internal sealed interface SearchContentState {
    data object Initial : SearchContentState
    data object Loading : SearchContentState
    data object Empty : SearchContentState
    data object Error : SearchContentState

    data class Success(
        val stations: List<RadioStation>,
        val isLoadingMore: Boolean = false,
    ) : SearchContentState
}
