package com.mordva.feature.home.state

internal data class HomeScreenState(
    val cityState: HomeScreenCityState = HomeScreenCityState.Loading,
    val radioState: HomeScreenRadioState = HomeScreenRadioState.Loading,
    val recommendationRadios: List<HomeScreenRadioState> = emptyList(),
    val isPlayRadio: Boolean = false,
)