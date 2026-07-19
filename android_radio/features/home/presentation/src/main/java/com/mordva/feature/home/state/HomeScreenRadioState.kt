package com.mordva.feature.home.state

import com.mordva.domain.domain.model.RadioStation

internal sealed interface HomeScreenRadioState {
    data object Error : HomeScreenRadioState

    data object Loading : HomeScreenRadioState

    data class Success(
        val maxValue: Float = 0f,
        val currentValue: Float = 0f,
        val station: RadioStation,
    ) : HomeScreenRadioState
}

internal fun HomeScreenRadioState.getStationId(): Int? {
    return (this as? HomeScreenRadioState.Success)?.station?.id
}