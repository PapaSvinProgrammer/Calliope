package com.mordva.feature.home.design.progressbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import com.mordva.system_ui.Resources

@Composable
internal fun CustomProgressBar(
    currentValue: Float,
    maxValue: Float,
    title: String,
    modifier: Modifier = Modifier,
    enabledDrag: Boolean = false,
    onValueChange: (Float) -> Unit = {},
) {
    val progress = if (maxValue > 0f) {
        (currentValue / maxValue).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress, animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    )

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val progressColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)

    val latestOnValueChange by rememberUpdatedState(onValueChange)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .drawBehind {
                drawRect(
                    color = backgroundColor,
                    size = size,
                )

                clipRect(right = size.width * animatedProgress) {
                    drawRect(
                        color = progressColor,
                        size = size,
                    )
                }
            }
            .then(
                if (enabledDrag && maxValue > 0f) {
                    Modifier.addProgressPointerInput(
                        maxValue = maxValue,
                        onValueChange = latestOnValueChange,
                    )
                } else {
                    Modifier
                }
            )
            .padding(
                vertical = Resources.Dimens.DP10,
                horizontal = Resources.Dimens.DP24,
            ),
        contentAlignment = Alignment.Center,
    ) {

        Text(
            text = title,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        )
    }
}

private fun Modifier.addProgressPointerInput(
    maxValue: Float,
    onValueChange: (Float) -> Unit,
): Modifier = pointerInput(maxValue) {
    fun updateValueByX(x: Float) {
        val width = size.width.toFloat()
        if (width <= 0f) return

        val newProgress = (x / width).coerceIn(0f, 1f)
        val newValue = newProgress * maxValue
        onValueChange(newValue)
    }

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        updateValueByX(down.position.x)
        down.consume()

        drag(down.id) { change: PointerInputChange ->
            updateValueByX(change.position.x)
            change.consume()
        }
    }
}
