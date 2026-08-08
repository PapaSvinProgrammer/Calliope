package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

internal sealed interface FilterAction {
    data object OnListEnded : FilterAction
    data object OnApplyClick : FilterAction
    data object OnResetClick : FilterAction
    data class OnLocationClick(val item: City) : FilterAction
    data class OnSearchTextChanged(val text: String) : FilterAction
}