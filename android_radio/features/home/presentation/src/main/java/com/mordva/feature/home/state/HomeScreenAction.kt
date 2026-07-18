package com.mordva.feature.home.state

internal sealed interface HomeScreenAction {
    data object OnSearchClick : HomeScreenAction
    data object OnPlayClick : HomeScreenAction
    data class OnPagerItemClick(val page: Int) : HomeScreenAction
}