package com.mordva.system_ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun OverlappingImages(
    images: List<String>,
    modifier: Modifier = Modifier,
    offset: Dp = Resources.Dimens.DP12,
    imageWidth: Dp = Resources.Dimens.DP24,
    aspectRatio: Float = 1f,
    shape: Shape = CircleShape,
    borderWidth: Dp = Resources.Dimens.DP1,
    borderColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val imageHeight = imageWidth / aspectRatio

    val containerWidth = if (images.isEmpty()) {
        0.dp
    } else {
        imageWidth + offset * (images.size - 1)
    }

    Box(
        modifier = modifier.size(
            width = containerWidth,
            height = imageHeight,
        ),
    ) {
        images.forEachIndexed { index, image ->
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier
                    .offset(x = offset * index)
                    .width(imageWidth)
                    .aspectRatio(aspectRatio)
                    .clip(shape)
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = shape,
                    ),
            )
        }
    }
}

