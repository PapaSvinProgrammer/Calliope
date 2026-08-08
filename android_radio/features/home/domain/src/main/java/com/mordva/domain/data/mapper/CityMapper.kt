package com.mordva.domain.data.mapper

import com.mordva.domain.data.model.CityDto
import com.mordva.domain.domain.model.City

internal fun CityDto.toDomain() = City(
    id = id,
    title = title,
    regionTitle = regionTitle,
    cityImageUrl = cityImage?.imageUrl,
    regionImageUrl = regionImage?.imageUrl,
)

internal fun List<CityDto>.toDomain() = map { it.toDomain() }
