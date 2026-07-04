package com.mordva.feature.home.component.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.mordva.system_ui.Resources
import com.mordva.system_ui.shimmer.shimmer
import kotlin.math.absoluteValue

@Composable
internal fun ShimmerRadioPagerItem(
    pageOffset: Float,
    modifier: Modifier = Modifier,
) {
    val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
    val centerProgress = 1f - absoluteOffset

    val contentAlpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = centerProgress,
    )

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .graphicsLayer { alpha = contentAlpha }
            .clip(RoundedCornerShape(Resources.Dimens.DP16))
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(Resources.Dimens.DP16),
            )
            .shimmer()
            .padding(
                vertical = Resources.Dimens.DP16,
                horizontal = Resources.Dimens.DP8,
            ),
    )
}