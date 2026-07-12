package com.mordva.feature.home.component.pager

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp

@Stable
internal data class RadioPageTransform(
    val rotationY: Float,
    val scale: Float,
    val alpha: Float,
    val translationX: Dp,
    val transformOrigin: TransformOrigin,
)