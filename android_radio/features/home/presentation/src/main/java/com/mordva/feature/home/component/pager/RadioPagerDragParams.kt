package com.mordva.feature.home.component.pager

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.mordva.feature.home.R

internal data class RadioPagerDragParams(
    val liftOnPress: Dp,
    val targetOffsetY: Dp,
    val borderFillDurationMillis: Int,
    val dragEndBorderDurationMillis: Int,
    val dragCancelBorderDurationMillis: Int,
    val dragThreshold: Float,
)

private const val BORDER_FILL_DURATION_MILLIS = 1000
private const val DRAG_END_BORDER_DURATION_MILLIS = 700
private const val DRAG_CANCEL_BORDER_DURATION_MILLIS = 150
private const val DRAG_THRESHOLD = 0.85f

@Composable
internal fun rememberRadioPagerDragParams(): RadioPagerDragParams = RadioPagerDragParams(
    liftOnPress = dimensionResource(R.dimen.radio_pager_lift_on_press),
    targetOffsetY = dimensionResource(R.dimen.radio_pager_drag_target_offset_y),
    borderFillDurationMillis = BORDER_FILL_DURATION_MILLIS,
    dragEndBorderDurationMillis = DRAG_END_BORDER_DURATION_MILLIS,
    dragCancelBorderDurationMillis = DRAG_CANCEL_BORDER_DURATION_MILLIS,
    dragThreshold = DRAG_THRESHOLD,
)
