package com.mordva.domain.location.data.mapper

import com.mordva.domain.location.data.model.CityDto
import com.mordva.domain.location.domain.model.City
import kotlin.collections.map

internal fun CityDto.toDomain() = City(
    id = id,
    title = title,
    regionTitle = regionTitle,
    cityImageUrl = cityImage?.imageUrl,
    regionImageUrl = regionImage?.imageUrl,
)

internal fun List<CityDto>.toDomain() = map { it.toDomain() }
