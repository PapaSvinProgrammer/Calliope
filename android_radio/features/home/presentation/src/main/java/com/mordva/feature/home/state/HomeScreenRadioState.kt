package com.mordva.feature.home.state

internal sealed interface HomeScreenRadioState {
    data object Error : HomeScreenRadioState

    data object Loading : HomeScreenRadioState

    data class Success(
        val maxValue: Float,
        val currentValue: Float,
        val title: String,
        val imageUrl: String,
    ) : HomeScreenRadioState
}