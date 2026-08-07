package com.mordva.feature.home.utils

import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.player.api.model.PlaybackState
internal fun PlaybackState.toRadioState(): HomeScreenRadioState {
    if (error != null) return HomeScreenRadioState.Error
    if (isLoading || !isConnected) return HomeScreenRadioState.Loading

    val id = currentTrackId?.toIntOrNull()
        ?: return if (error != null) HomeScreenRadioState.Error else HomeScreenRadioState.Loading

    return HomeScreenRadioState.Success(
        id = id,
        title = title.orEmpty(),
        description = artist.orEmpty(),
        artworkUrl = artworkUri?.toString(),
        elapsedMs = positionMs.coerceAtLeast(0L),
        durationMs = durationMs.takeIf { it > 0L },
    )
}
