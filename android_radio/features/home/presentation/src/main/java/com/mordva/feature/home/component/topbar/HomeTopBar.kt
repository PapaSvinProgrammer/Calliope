package com.mordva.feature.home.component.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.mordva.feature.home.R
import com.mordva.feature.home.state.HomeScreenCityState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(
    state: HomeScreenCityState,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(dimensionResource(R.dimen.top_bar_height))
            .fillMaxWidth(),
    ) {
        when (state) {
            HomeScreenCityState.Error -> TODO()
            HomeScreenCityState.Loading -> {
                ShimmerLocationTopBarContent()
            }

            is HomeScreenCityState.Success -> {
                LocationTopBarContent(
                    images = listOf(state.emblemUrl, state.flagUrl!!),
                    title = state.title,
                )
            }
        }
    }
}