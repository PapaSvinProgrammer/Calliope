package com.mordva.presentation.state

internal sealed interface FilterEvent {
    data object SendLoadMoreError : FilterEvent
}