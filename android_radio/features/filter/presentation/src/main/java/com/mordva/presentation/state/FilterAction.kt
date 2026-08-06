package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

internal sealed interface FilterAction {
    data object OnListEnded : FilterAction
    data class OnItemClick(val item: City) : FilterAction
    data class OnSearchTextChanged(val text: String) : FilterAction
}