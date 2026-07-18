package com.mordva.feature.home.component.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.mordva.system_ui.Resources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Card inside the pager.
 *
 * Long press:
 *  1. Запускает динамическое заполнение border из BottomCenter примерно на 2 секунды.
 *  2. После заполнения border карточка поднимается.
 *  3. Только после этого начинается обработка drag.
 *
 * Drag interaction:
 *  - Long press  → border loading animation → card lifts slightly up → drag mode begins
 *  - Drag ↑      → card follows finger vertically only
 *  - Release ≥ 85% of target → [onDragToTarget] called, card springs back
 *  - Release < 85%            → card springs back immediately
 *
 * While dragging, [onDragOffsetChanged] reports the current offsetY so
 * [RadioCoverPager] can render an unclipped overlay copy above the pager.
 */
@Composable
internal fun RadioPagerItem(
    id: Int,
    imageUrl: String,
    title: String,
    pageOffset: Float,
    onClick: () -> Unit,
    onDragToTarget: () -> Unit,
    onDragOffsetChanged: (offsetY: Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dragParams = rememberRadioPagerDragParams()

    val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
    val centerProgress = 1f - absoluteOffset

    val contentAlpha = lerp(0.5f, 1f, centerProgress)
    val borderWidth = lerp(Resources.Dimens.ZERO, Resources.Dimens.DP2, centerProgress)

    val liftOnPressPx = with(density) { dragParams.liftOnPress.toPx() }
    val targetOffsetYPx = with(density) { -dragParams.targetOffsetY.toPx() }

    val offsetY = remember(id) { Animatable(0f) }
    val dynamicBorderProgress = remember(id) { Animatable(0f) }

    RadioPagerItemContent(
        imageUrl = imageUrl,
        title = title,
        contentAlpha = contentAlpha,
        borderWidth = borderWidth,
        centerProgress = centerProgress,
        dynamicBorderProgress = dynamicBorderProgress.value,
        modifier = modifier.verticalLongPressDrag(
            offsetY = offsetY,
            dynamicBorderProgress = dynamicBorderProgress,
            liftOnPress = liftOnPressPx,
            targetOffsetY = targetOffsetYPx,
            borderFillDurationMillis = dragParams.borderFillDurationMillis,
            dragEndBorderDurationMillis = dragParams.dragEndBorderDurationMillis,
            dragCancelBorderDurationMillis = dragParams.dragCancelBorderDurationMillis,
            dragThreshold = dragParams.dragThreshold,
            scope = scope,
            onDragOffsetChanged = onDragOffsetChanged,
            onDragToTarget = onDragToTarget,
        ),
    )
}

private fun Modifier.verticalLongPressDrag(
    offsetY: Animatable<Float, *>,
    dynamicBorderProgress: Animatable<Float, *>,
    liftOnPress: Float,
    targetOffsetY: Float,
    borderFillDurationMillis: Int,
    dragEndBorderDurationMillis: Int,
    dragCancelBorderDurationMillis: Int,
    dragThreshold: Float,
    scope: CoroutineScope,
    onDragOffsetChanged: (Float?) -> Unit,
    onDragToTarget: () -> Unit,
): Modifier = pointerInput(Unit) {
    var dragEnabled = false
    var longPressSequenceActive: Boolean

    detectDragGesturesAfterLongPress(
        onDragStart = {
            longPressSequenceActive = true
            dragEnabled = false

            scope.launch {
                offsetY.snapTo(0f)
                dynamicBorderProgress.snapTo(0f)

                dynamicBorderProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = borderFillDurationMillis,
                        easing = LinearEasing,
                    ),
                )

                if (!longPressSequenceActive) return@launch

                offsetY.animateTo(
                    targetValue = -liftOnPress,
                    animationSpec = spring(),
                )
                onDragOffsetChanged(offsetY.value)

                dragEnabled = true
            }
        },
        onDrag = { change, dragAmount ->
            change.consume()
            if (!dragEnabled) return@detectDragGesturesAfterLongPress
            scope.launch {
                val newValue = (offsetY.value + dragAmount.y).coerceIn(targetOffsetY, 0f)
                offsetY.snapTo(newValue)
                onDragOffsetChanged(offsetY.value)
            }
        },
        onDragEnd = {
            longPressSequenceActive = false
            dragEnabled = false

            scope.launch {
                dynamicBorderProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = dragEndBorderDurationMillis),
                )

                if (offsetY.value <= targetOffsetY * dragThreshold) {
                    offsetY.animateTo(
                        targetValue = targetOffsetY,
                        animationSpec = spring(),
                    )
                    onDragToTarget()
                }

                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(),
                )
                onDragOffsetChanged(null)
            }
        },
        onDragCancel = {
            longPressSequenceActive = false
            dragEnabled = false

            scope.launch {
                dynamicBorderProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = dragCancelBorderDurationMillis),
                )

                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(),
                )
                onDragOffsetChanged(null)
            }
        },
    )
}

@Composable
internal fun RadioPagerItemContent(
    imageUrl: String,
    title: String,
    contentAlpha: Float,
    borderWidth: Dp,
    centerProgress: Float,
    dynamicBorderProgress: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Resources.Dimens.DP16)

    val staticBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
        alpha = centerProgress,
    )

    val dynamicBorderColor = MaterialTheme.colorScheme.primary

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
                staticBorderColor = staticBorderColor,
                dynamicBorderWidth = Resources.Dimens.DP2,
                dynamicBorderColor = dynamicBorderColor,
                cornerRadius = Resources.Dimens.DP16,
                progress = dynamicBorderProgress,
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

/**
 * RoundedRect path со стартовой точкой строго в BottomCenter.
 */
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
        quadraticBezierTo(
            x1 = rect.left,
            y1 = rect.bottom,
            x2 = rect.left,
            y2 = rect.bottom - r,
        )

        lineTo(
            x = rect.left,
            y = rect.top + r,
        )
        quadraticBezierTo(
            x1 = rect.left,
            y1 = rect.top,
            x2 = rect.left + r,
            y2 = rect.top,
        )

        lineTo(
            x = rect.right - r,
            y = rect.top,
        )
        quadraticBezierTo(
            x1 = rect.right,
            y1 = rect.top,
            x2 = rect.right,
            y2 = rect.top + r,
        )

        lineTo(
            x = rect.right,
            y = rect.bottom - r,
        )
        quadraticBezierTo(
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
