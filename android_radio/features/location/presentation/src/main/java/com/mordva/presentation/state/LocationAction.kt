package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

internal sealed interface LocationAction {
    data object OnListEnded : LocationAction
    data class OnItemClick(val item: City) : LocationAction
    data class OnSearchTextChanged(val text: String) : LocationAction
}