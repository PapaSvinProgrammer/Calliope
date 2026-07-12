package com.mordva.control_bar

sealed interface BottomControlsAction {
    sealed interface Control : BottomControlsAction {
        data class OnItemClick(val item: ControlItem) : Control
    }

    sealed interface Search : BottomControlsAction {
        data object OnCloseClick : Search
        data object OnSearchClick : Search
        data class OnTextChanged(val value: String) : Search
    }
}