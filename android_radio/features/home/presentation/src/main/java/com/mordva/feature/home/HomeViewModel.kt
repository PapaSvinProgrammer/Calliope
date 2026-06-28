package com.mordva.feature.home

import androidx.lifecycle.ViewModel
import com.mordva.feature.home.state.HomeScreenCityState
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

internal class HomeViewModel : ViewModel() {
    private val isPlayRadioState = MutableStateFlow(false)
    private val state = MutableStateFlow(false)

    val uiState: Flow<HomeScreenState> = combine(
        isPlayRadioState,
        state,
    ) { isPlayRadio, state ->
        HomeScreenState(
            radioState = HomeScreenRadioState.Success(
                maxValue = 100f,
                currentValue = 40f,
                title = "Comedy Radio",
                imageUrl = "https://i.ytimg.com/vi/DIUzoDj0STc/hq720.jpg?sqp=-oaymwEXCNUGEOADIAQqCwjVARCqCBh4INgESFo&amp;rs=AMzJL3me5GJIFP9wrXOSLUV_HM6VazclvQ",
            ),
            cityState = HomeScreenCityState.Success(
                emblemUrl = "https://www.ph4.ru/DL/HERALD/CITIES/ru/arms_achinsk.gif",
                flagUrl = "https://www.ph4.ru/DL/HERALD/COUNTRIES/ru/flags_krasnodar.gif",
                title = "Москва"
            ),
            isPlayRadio = isPlayRadio,
        )
    }

    fun togglePlayRadio() {
        isPlayRadioState.value = !isPlayRadioState.value
    }
}