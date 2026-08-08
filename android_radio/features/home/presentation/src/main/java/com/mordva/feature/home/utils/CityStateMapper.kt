package com.mordva.feature.home.utils

import com.mordva.datastore.api.model.CityData
import com.mordva.feature.home.state.HomeScreenCityState

internal fun CityData.toUiState(): HomeScreenCityState {
    return if (id == -1) {
        HomeScreenCityState.Error
    } else {
        HomeScreenCityState.Success(
            regionImageUrl = regionImageUrl,
            cityImageUrl = cityImageUrl,
            title = title,
        )
    }
}