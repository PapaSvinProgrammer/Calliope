package com.mordva.feature.home.state

sealed interface HomeScreenEvent {
    data class MovePager(val position: Int) : HomeScreenEvent
    data object ShowSelectStationErrorMessage : HomeScreenEvent
    data object ShowLoadMoreErrorMessage : HomeScreenEvent
}
