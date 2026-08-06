package com.mordva.radio.presentation

import com.mordva.control_bar.ControlItem
import com.mordva.system_ui.sheet.AppSheet

data class MainUiState(
    val searchExpanded: Boolean = false,
    val searchText: String = "",
    val selectedItem: ControlItem = ControlItem.HOME,
    val currentSheet: AppSheet? = null,
)