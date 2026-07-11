package com.mordva.radio.presentation

import com.mordva.control_bar.ControlItem

data class MainUiState(
    val searchExpanded: Boolean = false,
    val searchText: String = "",
    val selectedItem: ControlItem = ControlItem.HOME,
)