package com.mordva.presentation.state

internal sealed interface LocationEvent {
    data object SendLoadMoreError : LocationEvent
}