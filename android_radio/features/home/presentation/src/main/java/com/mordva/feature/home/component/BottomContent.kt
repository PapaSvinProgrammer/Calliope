package com.mordva.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mordva.feature.home.component.pager.RadioCoverPager
import com.mordva.feature.home.state.HomeScreenRadioState

@Composable
internal fun BottomContent(
    radios: List<HomeScreenRadioState>,
    modifier: Modifier = Modifier,
) {
    RadioCoverPager(
        items = radios,
        modifier = modifier,
    )
}

