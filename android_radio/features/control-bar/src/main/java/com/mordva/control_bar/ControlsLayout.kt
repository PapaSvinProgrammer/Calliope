package com.mordva.control_bar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mordva.system_ui.button.SpringIconButton

@Composable
internal fun ControlsLayout(
    selectedItem: ControlItem,
    onClick: (ControlItem) -> Unit,
    modifier: Modifier = Modifier,
    internalPadding: PaddingValues = PaddingValues(),
) {
    val density = LocalDensity.current
    var containerHeight by remember { mutableStateOf(0.dp) }

    val itemPositions = remember { mutableStateMapOf<ControlItem, Offset>() }
    val itemSizes = remember { mutableStateMapOf<ControlItem, IntSize>() }

    val selectedPosition = itemPositions[selectedItem] ?: Offset.Zero
    val selectedSize = itemSizes[selectedItem] ?: IntSize.Zero

    val selectedExtraWidth = dimensionResource(R.dimen.selected_extra_padding)

    val backgroundOffsetX by animateDpAsState(
        targetValue = with(density) { selectedPosition.x.toDp() } - selectedExtraWidth / 2,
        animationSpec = createAnimationSpec()
    )

    val backgroundOffsetY by animateDpAsState(
        targetValue = with(density) { selectedPosition.y.toDp() },
        animationSpec = createAnimationSpec()
    )

    val backgroundWidth by animateDpAsState(
        targetValue = with(density) { selectedSize.width.toDp() } + selectedExtraWidth,
        animationSpec = createAnimationSpec()
    )

    val backgroundHeight by animateDpAsState(
        targetValue = containerHeight,
        animationSpec = createAnimationSpec()
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = CircleShape,
            )
            .onGloballyPositioned { coordinates ->
                containerHeight = with(density) { coordinates.size.height.toDp() }
            }
    ) {
        if (selectedSize != IntSize.Zero) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = backgroundOffsetX.roundToPx(),
                            y = backgroundOffsetY.roundToPx(),
                        )
                    }
                    .size(
                        width = backgroundWidth,
                        height = backgroundHeight
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    )
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(internalPadding)
                .fillMaxWidth(),
        ) {
            SpringIconButton(
                onClick = { onClick(ControlItem.HOME) },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    itemPositions[ControlItem.HOME] = coordinates.positionInParent()
                    itemSizes[ControlItem.HOME] = coordinates.size
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_home),
                    contentDescription = null,
                )
            }

            SpringIconButton(
                onClick = { onClick(ControlItem.DOWNLOADS) },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    itemPositions[ControlItem.DOWNLOADS] = coordinates.positionInParent()
                    itemSizes[ControlItem.DOWNLOADS] = coordinates.size
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark),
                    contentDescription = null,
                )
            }

            SpringIconButton(
                onClick = { onClick(ControlItem.SETTINGS) },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    itemPositions[ControlItem.SETTINGS] = coordinates.positionInParent()
                    itemSizes[ControlItem.SETTINGS] = coordinates.size
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                    contentDescription = null,
                )
            }
        }
    }
}

private fun <T> createAnimationSpec(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)