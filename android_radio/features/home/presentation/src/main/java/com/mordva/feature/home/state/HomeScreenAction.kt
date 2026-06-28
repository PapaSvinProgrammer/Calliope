package com.mordva.feature.home.state

internal sealed interface HomeScreenAction {
    data object OnSearchClick : HomeScreenAction
}