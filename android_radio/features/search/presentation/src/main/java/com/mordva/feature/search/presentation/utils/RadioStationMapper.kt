package com.mordva.feature.search.presentation.utils

import com.mordva.domain.domain.model.RadioStation
import com.mordva.player.api.model.AudioItem
import kotlin.text.isNotBlank

internal fun RadioStation.toAudioItem() = AudioItem(
    id = id.toString(),
    uri = streamUrl,
    title = title,
    artist = description.takeIf(String::isNotBlank),
    artworkUri = imageUrl.takeIf(String::isNotBlank),
)