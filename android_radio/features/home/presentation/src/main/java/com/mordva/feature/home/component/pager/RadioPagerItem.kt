package com.mordva.feature.home.component.pager

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.mordva.system_ui.Resources
import kotlin.math.absoluteValue

@Composable
internal fun RadioPagerItem(
    id: Int,
    imageUrl: String,
    imageWidth: Int?,
    imageHeight: Int?,
    title: String,
    pageOffset: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
    val centerProgress = 1f - absoluteOffset

    val contentAlpha = lerp(0.5f, 1f, centerProgress)
    val borderWidth = lerp(Resources.Dimens.ZERO, Resources.Dimens.DP2, centerProgress)

    val dynamicBorderProgress = remember(id) { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            dynamicBorderProgress.snapTo(0f)
            dynamicBorderProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing,
                ),
            )
        } else {
            dynamicBorderProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800),
            )
        }
    }

    RadioPagerItemContent(
        imageUrl = imageUrl,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        title = title,
        contentAlpha = contentAlpha,
        borderWidth = borderWidth,
        dynamicBorderProgress = dynamicBorderProgress.value,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onClick,
            )
    )
}

@Composable
internal fun RadioPagerItemContent(
    imageUrl: String,
    imageWidth: Int?,
    imageHeight: Int?,
    title: String,
    contentAlpha: Float,
    borderWidth: Dp,
    dynamicBorderProgress: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Resources.Dimens.DP16)
    val dynamicBorderColor = MaterialTheme.colorScheme.primary
    val imageAspectRatio = if (imageWidth != null && imageHeight != null && imageHeight > 0) {
        imageWidth.toFloat() / imageHeight
    } else {
        1f
    }
    val isNearlySquareImage = imageAspectRatio in 0.8f..1.25f
    Log.d("RRRR", "imageUrl = $imageUrl; imageWidth = $imageWidth; imageHeight = $imageHeight")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                alpha = contentAlpha
            }
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = shape,
            )
            .drawDynamicBorderFromBottomCenter(
                staticBorderWidth = borderWidth,
                staticBorderColor = Color.Transparent,
                dynamicBorderWidth = Resources.Dimens.DP2,
                dynamicBorderColor = dynamicBorderColor,
                cornerRadius = Resources.Dimens.DP16,
                progress = dynamicBorderProgress,
            )
            .padding(bottom = Resources.Dimens.DP16),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = if (isNearlySquareImage) ContentScale.Crop else ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape),
        )

        Spacer(modifier = Modifier.height(Resources.Dimens.DP2))

        Text(
            text = title,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Рисует:
 *  1. Обычный static border.
 *  2. Dynamic border, который заполняется из BottomCenter.
 *
 * Сейчас dynamic border расходится симметрично в две стороны от нижнего центра.
 */
private fun Modifier.drawDynamicBorderFromBottomCenter(
    staticBorderWidth: Dp,
    staticBorderColor: Color,
    dynamicBorderWidth: Dp,
    dynamicBorderColor: Color,
    cornerRadius: Dp,
    progress: Float,
): Modifier = drawBehind {
    val radiusPx = cornerRadius.toPx()

    val staticStrokeWidthPx = staticBorderWidth.toPx()

    if (staticStrokeWidthPx > 0f) {
        val staticInset = staticStrokeWidthPx / 2f

        drawRoundRect(
            color = staticBorderColor,
            topLeft = Offset(
                x = staticInset,
                y = staticInset,
            ),
            size = Size(
                width = size.width - staticStrokeWidthPx,
                height = size.height - staticStrokeWidthPx,
            ),
            cornerRadius = CornerRadius(
                x = radiusPx,
                y = radiusPx,
            ),
            style = Stroke(width = staticStrokeWidthPx),
        )
    }

    val dynamicProgress = progress.coerceIn(0f, 1f)
    if (dynamicProgress <= 0f) return@drawBehind

    val dynamicStrokeWidthPx = dynamicBorderWidth.toPx()
    val dynamicInset = dynamicStrokeWidthPx / 2f

    val rect = Rect(
        left = dynamicInset,
        top = dynamicInset,
        right = size.width - dynamicInset,
        bottom = size.height - dynamicInset,
    )

    val path = roundedRectPathStartingFromBottomCenter(
        rect = rect,
        radius = radiusPx,
    )

    val measure = PathMeasure()
    measure.setPath(path, false)

    val totalLength = measure.length
    val visibleLength = totalLength * dynamicProgress
    val halfVisibleLength = visibleLength / 2f

    val destination = Path()

    /**
     * Так как path начинается в BottomCenter и идет против часовой стрелки,
     * первый сегмент рисует одну сторону, второй сегмент — вторую сторону.
     *
     * В итоге border заполняется в две стороны от BottomCenter.
     */
    measure.getSegment(
        startDistance = 0f,
        stopDistance = halfVisibleLength,
        destination = destination,
        startWithMoveTo = true,
    )

    measure.getSegment(
        startDistance = totalLength - halfVisibleLength,
        stopDistance = totalLength,
        destination = destination,
        startWithMoveTo = true,
    )

    drawPath(
        path = destination,
        color = dynamicBorderColor,
        style = Stroke(
            width = dynamicStrokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private fun roundedRectPathStartingFromBottomCenter(
    rect: Rect,
    radius: Float,
): Path {
    val r = radius.coerceAtMost(
        minOf(rect.width, rect.height) / 2f,
    )

    return Path().apply {
        moveTo(
            x = rect.center.x,
            y = rect.bottom,
        )

        lineTo(
            x = rect.left + r,
            y = rect.bottom,
        )
        quadraticTo(
            x1 = rect.left,
            y1 = rect.bottom,
            x2 = rect.left,
            y2 = rect.bottom - r,
        )

        lineTo(
            x = rect.left,
            y = rect.top + r,
        )
        quadraticTo(
            x1 = rect.left,
            y1 = rect.top,
            x2 = rect.left + r,
            y2 = rect.top,
        )

        lineTo(
            x = rect.right - r,
            y = rect.top,
        )
        quadraticTo(
            x1 = rect.right,
            y1 = rect.top,
            x2 = rect.right,
            y2 = rect.top + r,
        )

        lineTo(
            x = rect.right,
            y = rect.bottom - r,
        )
        quadraticTo(
            x1 = rect.right,
            y1 = rect.bottom,
            x2 = rect.right - r,
            y2 = rect.bottom,
        )

        lineTo(
            x = rect.center.x,
            y = rect.bottom,
        )
    }
}
