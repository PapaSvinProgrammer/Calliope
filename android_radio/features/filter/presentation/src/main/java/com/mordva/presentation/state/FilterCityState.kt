package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

sealed interface LocationCityState {
    data object Init : LocationCityState

    data class Success(
        val items: List<City>
    ) : LocationCityState

    data object Loading : LocationCityState
    data object Error : LocationCityState
}

internal fun LocationCityState.getItems(): List<City> =
    (this as? LocationCityState.Success)?.items.orEmpty()