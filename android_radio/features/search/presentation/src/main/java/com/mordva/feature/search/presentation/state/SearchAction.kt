package com.mordva.feature.search.presentation.state

import com.mordva.domain.domain.model.RadioStation

internal sealed interface SearchAction {
    data class OnSearchTextChanged(val text: String) : SearchAction
    data class OnStationClick(val station: RadioStation) : SearchAction
    data object OnListEnded : SearchAction
    data object OnRetryClick : SearchAction
}
