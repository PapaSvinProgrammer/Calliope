package com.mordva.feature.home.state

internal data class HomeScreenState(
    val cityState: HomeScreenCityState = HomeScreenCityState.Loading,
    val radioState: HomeScreenRadioState = HomeScreenRadioState.Loading,
    val isPlayRadio: Boolean = false,
)