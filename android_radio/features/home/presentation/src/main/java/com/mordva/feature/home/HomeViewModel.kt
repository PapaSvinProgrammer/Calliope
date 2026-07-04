package com.mordva.feature.home

import androidx.lifecycle.ViewModel
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenCityState
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.collections.listOf

internal class HomeViewModel : ViewModel() {
    private val isPlayRadioState = MutableStateFlow(false)
    private val state = MutableStateFlow(false)

    val uiState: Flow<HomeScreenState> = combine(
        isPlayRadioState,
        state,
    ) { isPlayRadio, state ->
        HomeScreenState(
//            radioState = HomeScreenRadioState.Loading,
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
            recommendationRadios = listOf(
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Первое радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                ),
                HomeScreenRadioState.Loading,
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Второе радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                ),
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Третье радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                ),
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Четвёртое радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                ),
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Пятое радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                ),
                HomeScreenRadioState.Success(
                    maxValue = 1f,
                    currentValue = 1f,
                    title = "Шестое радио",
                    imageUrl = "https://comicbook.com/wp-content/uploads/sites/4/2025/03/Invincible-Season-3-Episode-8-Finale-Reactions.jpeg?w=819"
                )
            )
        )
    }

    fun onActionHandle(action: HomeScreenAction) = when (action) {
        HomeScreenAction.OnPlayClick -> togglePlayRadio()
        HomeScreenAction.OnSearchClick -> TODO()
    }

    private fun togglePlayRadio() {
        isPlayRadioState.value = !isPlayRadioState.value
    }
}