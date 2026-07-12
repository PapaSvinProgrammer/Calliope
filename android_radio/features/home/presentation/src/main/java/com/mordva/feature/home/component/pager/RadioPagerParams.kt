package com.mordva.feature.home.component.pager

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp

@Stable
internal data class RadioPagerParams(
    val pageWidth: Dp,
    val edgeContentPadding: Dp,
    val overlap: Dp,
)