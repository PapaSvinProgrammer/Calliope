package com.mordva.feature.home.design.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.mordva.feature.home.state.HomeScreenCityState
import com.mordva.system_ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(
    state: HomeScreenCityState,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { RenderLocationTobBarContent(state) },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                    contentDescription = null,
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun RenderLocationTobBarContent(
    state: HomeScreenCityState,
) {
    when (state) {
        HomeScreenCityState.Error -> TODO()
        HomeScreenCityState.Loading -> Text("ZALUPA")
        is HomeScreenCityState.Success -> {
            LocationTopBarContent(
                images = listOf(state.emblemUrl, state.flagUrl!!),
                title = state.title,
            )
        }
    }
}