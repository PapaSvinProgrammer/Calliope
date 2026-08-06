package com.mordva.presentation.utils

import com.mordva.datastore.api.model.CityData
import com.mordva.domain.location.domain.model.City

internal fun CityData.toUiState(): City? {
    return if (id != -1) {
        City(
            id = id,
            title = title,
            regionTitle = regionTitle,
            cityImageUrl = cityImageUrl,
            regionImageUrl = regionImageUrl,
        )
    } else {
        null
    }
}

internal fun City.toData() = CityData(
    id = id,
    title = title,
    regionTitle = regionTitle.orEmpty(),
    cityImageUrl = cityImageUrl,
    regionImageUrl = regionImageUrl,
)