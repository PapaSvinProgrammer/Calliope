package com.mordva.feature.home.utils

import com.mordva.domain.domain.model.RadioStation
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.player.api.model.AudioItem

internal fun RadioStation.toRadioState() = HomeScreenRadioState.Success(
    id = id,
    title = title,
    description = description,
    artworkUrl = imageUrl,
    artworkWidth = imageWidth,
    artworkHeight = imageHeight,
    streamUrl = streamUrl,
)

internal fun HomeScreenRadioState.Success.toAudioItem() = AudioItem(
    id = id.toString(),
    uri = streamUrl,
    title = title,
    artist = description.takeIf(String::isNotBlank),
    artworkUri = artworkUrl?.takeIf(String::isNotBlank),
)
