package com.mordva.feature.home.component.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.mordva.system_ui.Resources
import kotlin.math.absoluteValue

@Composable
internal fun RadioPagerItem(
    imageUrl: String,
    title: String,
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

    val borderWidth = lerp(
        start = Resources.Dimens.ZERO,
        stop = Resources.Dimens.DP2,
        fraction = centerProgress,
    )

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { alpha = contentAlpha }
                .clip(RoundedCornerShape(Resources.Dimens.DP16))
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(Resources.Dimens.DP16),
                )
                .border(
                    width = borderWidth,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(centerProgress),
                    shape = RoundedCornerShape(Resources.Dimens.DP16),
                )
                .padding(
                    top = Resources.Dimens.DP8,
                    bottom = Resources.Dimens.DP16,
                    start = Resources.Dimens.DP8,
                    end = Resources.Dimens.DP8,
                ),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Resources.Dimens.DP16)),
            )

            Spacer(modifier = Modifier.height(Resources.Dimens.DP10))

            Text(
                text = title,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}