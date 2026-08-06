package com.mordva.radio.domain

import androidx.lifecycle.ViewModel
import com.mordva.control_bar.ControlItem
import com.mordva.radio.presentation.MainUiState
import com.mordva.system_ui.sheet.AppSheet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val searchExpandedState = MutableStateFlow(false)
    private val searchTextState = MutableStateFlow("")
    private val selectedItemState = MutableStateFlow(ControlItem.HOME)
    private val currentSheetState = MutableStateFlow<AppSheet?>(null)

    val uiState = combine(
        searchExpandedState,
        searchTextState,
        selectedItemState,
        currentSheetState,
    ) { searchExpanded, searchText, selectedItem, currentSheet ->
        MainUiState(
            searchExpanded = searchExpanded,
            searchText = searchText,
            selectedItem = selectedItem,
            currentSheet = currentSheet,
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

    fun updateSheetState(sheet: AppSheet?) {
        currentSheetState.value = sheet
    }
}