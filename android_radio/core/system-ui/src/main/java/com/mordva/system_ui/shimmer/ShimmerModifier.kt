package com.mordva.system_ui.shimmer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

private const val SHIMMER_DURATION_MILLIS = 1_500
private const val SHIMMER_INITIAL_OFFSET = -1_000f
private const val SHIMMER_TARGET_OFFSET = 1_000f
private const val SHIMMER_WIDTH_FRACTION = 2f

private val ShimmerBaseColor = Color(0xFFE0E0E0)
private val ShimmerHighlightColor = Color(0xFFF5F5F5)

fun Modifier.shimmer(
    isVisible: Boolean = true,
    baseColor: Color = ShimmerBaseColor,
    highlightColor: Color = ShimmerHighlightColor,
    durationMillis: Int = SHIMMER_DURATION_MILLIS,
): Modifier = composed {
    if (!isVisible) {
        return@composed this
    }

    val transition = rememberInfiniteTransition()

    val translateAnimation by transition.animateFloat(
        initialValue = SHIMMER_INITIAL_OFFSET,
        targetValue = SHIMMER_TARGET_OFFSET,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis),
            repeatMode = RepeatMode.Restart
        )
    )

    this.drawWithContent {
        drawContent()

        val shimmerWidth = size.width / SHIMMER_WIDTH_FRACTION

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.35f),
                    highlightColor.copy(alpha = 0.75f),
                    baseColor.copy(alpha = 0.35f),
                ),
                start = Offset(
                    x = translateAnimation - shimmerWidth,
                    y = 0f
                ),
                end = Offset(
                    x = translateAnimation + shimmerWidth,
                    y = size.height
                ),
                tileMode = TileMode.Clamp
            )
        )
    }
}
