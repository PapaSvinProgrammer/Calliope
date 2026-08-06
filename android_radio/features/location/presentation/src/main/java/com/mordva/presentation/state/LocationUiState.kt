package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

data class LocationUiState(
    val searchText: String = "",
    val cityListState: LocationCityState = LocationCityState.Loading,
    val currentCity: City? = null,
)