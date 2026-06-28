package com.mordva.feature.home.design.topbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.mordva.system_ui.Resources

@Composable
internal fun OverlappingCircleContents(
    images: List<String>,
    modifier: Modifier = Modifier,
    offset: Dp = Resources.Dimens.DP12,
) {
    Box(modifier = modifier) {
        images.forEachIndexed { index, image ->
            Box(modifier = Modifier.offset(x = offset * index)) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Resources.Dimens.DP24)
                        .clip(CircleShape)
                        .border(
                            width = Resources.Dimens.DP1,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape,
                        )
                )
            }
        }
    }
}

