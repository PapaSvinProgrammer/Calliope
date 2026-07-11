package com.mordva.radio.domain

import androidx.lifecycle.ViewModel
import com.mordva.control_bar.ControlItem
import com.mordva.radio.presentation.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MainViewModel : ViewModel() {
    private val searchExpandedState = MutableStateFlow(false)
    private val searchTextState = MutableStateFlow("")
    private val selectedItemState = MutableStateFlow(ControlItem.HOME)

    val uiState = combine(
        searchExpandedState,
        searchTextState,
        selectedItemState,
    ) { searchExpanded, searchText, selectedItem ->
        MainUiState(
            searchExpanded = searchExpanded,
            searchText = searchText,
            selectedItem = selectedItem,
        )
    }

    fun controlItemHandle(item: ControlItem) {
        selectedItemState.value = item
    }

    fun closeClickHandle() {
        if (searchTextState.value.isEmpty()) {
            searchExpandedState.value = false
        }

        searchTextState.value = ""
    }

    fun searchClickHandle() {
        searchExpandedState.value = !searchExpandedState.value
    }

    fun searchTextChanged(text: String) {
        searchTextState.value = text
    }
}