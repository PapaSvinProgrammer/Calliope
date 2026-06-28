package com.mordva.feature.home.state

sealed interface HomeScreenCityState {
    data object Error : HomeScreenCityState

    data object Loading : HomeScreenCityState

    data class Success(
        val emblemUrl: String,
        val flagUrl: String?,
        val title: String,
    ) : HomeScreenCityState
}