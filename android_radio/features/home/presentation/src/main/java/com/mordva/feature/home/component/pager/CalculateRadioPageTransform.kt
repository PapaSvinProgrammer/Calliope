package com.mordva.feature.home.component.pager

import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

internal fun calculateRadioPageTransform(
    pageOffset: Float,
): RadioPageTransform {
    val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

    val maxRotation = 40f
    val minScale = 0.9f
    val minAlpha = 0.9f

    val scale = 1f - ((1f - minScale) * absoluteOffset)
    val alpha = 1f - ((1f - minAlpha) * absoluteOffset)

    val rotationY = when {
        pageOffset < 0f -> maxRotation * absoluteOffset
        pageOffset > 0f -> -maxRotation * absoluteOffset
        else -> 0f
    }

    val translationX = when {
        pageOffset < 0f -> 26.dp * absoluteOffset
        pageOffset > 0f -> (-26).dp * absoluteOffset
        else -> 0.dp
    }

    val transformOrigin = when {
        pageOffset < 0f -> TransformOrigin(
            pivotFractionX = 1f,
            pivotFractionY = 0.5f,
        )

        pageOffset > 0f -> TransformOrigin(
            pivotFractionX = 0f,
            pivotFractionY = 0.5f,
        )

        else -> TransformOrigin.Center
    }

    return RadioPageTransform(
        rotationY = rotationY,
        scale = scale,
        alpha = alpha,
        translationX = translationX,
        transformOrigin = transformOrigin,
    )
}