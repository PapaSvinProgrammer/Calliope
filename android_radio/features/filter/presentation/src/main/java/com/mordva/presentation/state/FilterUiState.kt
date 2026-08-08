package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

data class FilterUiState(
    val searchText: String = "",
    val cityListState: LocationCityState = LocationCityState.Loading,
    val selectedCities: List<City> = emptyList(),
)