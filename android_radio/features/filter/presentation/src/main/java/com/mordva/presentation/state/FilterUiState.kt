package com.mordva.presentation.state

import com.mordva.domain.location.domain.model.City

data class FilterUiState(
    val searchText: String = "",
    val cityListState: LocationCityState = LocationCityState.Loading,
    val selectedCities: List<City> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val categories: List<String> = DEFAULT_CATEGORIES,
    val filterType: FilterType = FilterType.Location,
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Pop",
            "Rock",
            "Jazz",
            "Classical",
            "Electronic",
            "Hip-hop",
            "News",
            "Talk",
            "Sport",
            "For kids",
        )
    }
}