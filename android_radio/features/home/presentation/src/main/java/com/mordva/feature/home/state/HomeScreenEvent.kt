package com.mordva.feature.home.state

internal sealed interface HomeScreenEvent {
    data class MovePager(val position: Int) : HomeScreenEvent
}