package com.mordva.feature.home.component.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.mordva.feature.home.R
import com.mordva.system_ui.Resources
import com.mordva.system_ui.shimmer.shimmer

@Composable
internal fun ShimmerLocationTopBarContent(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(dimensionResource(R.dimen.location_shimmer_width))
                .height(dimensionResource(R.dimen.location_shimmer_height))
                .padding(vertical = Resources.Dimens.DP5, horizontal = Resources.Dimens.DP10)
                .align(Alignment.Center)
                .clip(CircleShape)
                .shimmer(),
        )
    }
}