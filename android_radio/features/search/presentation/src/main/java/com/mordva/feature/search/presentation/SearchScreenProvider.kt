package com.mordva.feature.search.presentation

import androidx.compose.runtime.Composable

@Composable
fun SearchScreenProvider(
    query: String,
    onStationClick: (Int) -> Unit = {},
) {
    SearchScreen(
        query = query,
        onStationClick = onStationClick,
    )
}
