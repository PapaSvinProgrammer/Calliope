package com.mordva.feature.home.state

internal sealed interface HomeScreenCityState {
    data object Error : HomeScreenCityState

    data object Loading : HomeScreenCityState

    data class Success(
        val regionImageUrl: String?,
        val cityImageUrl: String?,
        val title: String,
    ) : HomeScreenCityState
}