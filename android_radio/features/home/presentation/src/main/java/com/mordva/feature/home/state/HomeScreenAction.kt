package com.mordva.feature.home.state

sealed interface HomeScreenAction {
    data object OnSearchClick : HomeScreenAction
    data object OnPlayClick : HomeScreenAction
    data class OnPagerItemClick(val page: Int) : HomeScreenAction
    data object OnPagerEnded : HomeScreenAction
}
