package com.mordva.radio.domain

import com.mordva.control_bar.BottomControlsAction

fun MainViewModel.action(action: BottomControlsAction) = when (action) {
    is BottomControlsAction.Control.OnItemClick -> controlItemHandle(action.item)
    BottomControlsAction.Search.OnCloseClick -> closeClickHandle()
    BottomControlsAction.Search.OnSearchClick -> searchClickHandle()
    is BottomControlsAction.Search.OnTextChanged -> searchTextChanged(action.value)
}
