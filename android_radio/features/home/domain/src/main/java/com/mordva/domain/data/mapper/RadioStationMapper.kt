package com.mordva.domain.data.mapper

import com.mordva.domain.data.model.RadioStationDto
import com.mordva.domain.domain.model.RadioStation

internal fun RadioStationDto.toDomain() = RadioStation(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    imageWidth = imageWidth,
    imageHeight = imageHeight,
    streamUrl = streamUrl,
)

internal fun List<RadioStationDto>.toDomain() = map { it.toDomain() }
