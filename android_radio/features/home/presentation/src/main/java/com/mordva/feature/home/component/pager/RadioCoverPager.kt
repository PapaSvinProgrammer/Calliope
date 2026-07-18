package com.mordva.feature.home.component.pager

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.system_ui.Resources
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Holds info about the card currently being dragged.
 * [offsetY]   — current drag offset in px (negative = upward)
 * [pageIndex] — which pager page is being dragged
 * [cardX]     — horizontal center of that card in the BoxWithConstraints coords (px)
 */
private data class DragState(
    val pageIndex: Int,
    val offsetY: Float,
    val cardX: Float,
    val cardWidth: Float,
    val item: HomeScreenRadioState.Success,
)

@Composable
internal fun RadioCoverPager(
    pagerState: PagerState,
    items: List<HomeScreenRadioState>,
    onClickPagerItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    var dragState by remember { mutableStateOf<DragState?>(null) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val params = calculateRadioPagerParams(
            containerWidth = maxWidth,
            horizontalPadding = Resources.Dimens.DP20,
            centralItemWidthFraction = 0.6f,
            overlap = Resources.Dimens.DP45,
        )

        HorizontalPager(
            state = pagerState,
            snapPosition = SnapPosition.Center,
            contentPadding = PaddingValues(horizontal = params.edgeContentPadding),
            pageSize = PageSize.Fixed(params.pageWidth),
            pageSpacing = -params.overlap,
            userScrollEnabled = dragState == null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        ) { page ->
            val item = items[page]
            val pageOffset = pagerState.calculatePageOffset(page)

            when (item) {
                HomeScreenRadioState.Error -> Unit

                HomeScreenRadioState.Loading -> {
                    ShimmerRadioPagerItem(
                        pageOffset = pageOffset,
                        modifier = Modifier.radioItemParams(pageOffset),
                    )
                }

                is HomeScreenRadioState.Success -> {
                    var cardX by remember { mutableFloatStateOf(0f) }
                    val isDragging = dragState?.pageIndex == page

                    Box(
                        modifier = Modifier
                            .radioItemParams(pageOffset)
                            .onGloballyPositioned { coords ->
                                cardX = coords.positionInParent().x + coords.size.width / 2f
                            },
                    ) {
                        RadioPagerItem(
                            id = item.station.id,
                            imageUrl = item.station.imageUrl,
                            title = item.station.title,
                            pageOffset = pageOffset,
                            onClick = { onClickPagerItem(page) },
                            onDragToTarget = { onClickPagerItem(page) },
                            onDragOffsetChanged = { offsetY ->
                                dragState = if (offsetY != null) {
                                    DragState(
                                        pageIndex = page,
                                        offsetY = offsetY,
                                        cardX = cardX,
                                        cardWidth = with(density) { params.pageWidth.toPx() },
                                        item = item,
                                    )
                                } else {
                                    null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f },
                        )
                    }
                }
            }
        }

        dragState?.let { state ->
            val cardWidthDp: Dp = with(density) { state.cardWidth.toDp() }
            val cardStartX: Dp = with(density) { (state.cardX - state.cardWidth / 2f).toDp() }

            val absoluteOffset = pagerState.calculatePageOffset(state.pageIndex).absoluteValue
            val centerProgress = (1f - absoluteOffset).coerceIn(0f, 1f)
            val contentAlpha = lerp(0.5f, 1f, centerProgress)
            val borderWidth = lerp(
                Resources.Dimens.ZERO.value,
                Resources.Dimens.DP2.value,
                centerProgress,
            ).dp

            RadioPagerItemContent(
                imageUrl = state.item.station.imageUrl,
                title = state.item.station.title,
                contentAlpha = contentAlpha,
                borderWidth = borderWidth,
                centerProgress = centerProgress,
                dynamicBorderProgress = 0f,
                modifier = Modifier
                    .width(cardWidthDp)
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = cardStartX.roundToPx(),
                            y = state.offsetY.roundToInt(),
                        )
                    },
            )
        }
    }
}

private fun Modifier.radioItemParams(pageOffset: Float): Modifier {
    return this
        .zIndex(calculateRadioPageZIndex(pageOffset))
        .graphicsLayer {
            val transform = calculateRadioPageTransform(pageOffset)

            rotationY = transform.rotationY
            scaleX = transform.scale
            scaleY = transform.scale
            alpha = transform.alpha
            translationX = transform.translationX.toPx()

            cameraDistance = 16f * density
            transformOrigin = transform.transformOrigin
        }
        .fillMaxWidth()
}

private fun PagerState.calculatePageOffset(page: Int): Float {
    return (page - currentPage) - currentPageOffsetFraction
}

private fun calculateRadioPageZIndex(pageOffset: Float): Float {
    val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
    return 10f - absoluteOffset
}

private fun calculateRadioPagerParams(
    containerWidth: Dp,
    horizontalPadding: Dp,
    centralItemWidthFraction: Float,
    overlap: Dp,
): RadioPagerParams {
    val maxPageWidth = containerWidth - horizontalPadding * 2
    val calculatedPageWidth = containerWidth * centralItemWidthFraction
    val pageWidth = calculatedPageWidth.coerceAtMost(maxPageWidth)
    val edgeContentPadding = ((containerWidth - pageWidth) / 2).coerceAtLeast(horizontalPadding)

    return RadioPagerParams(
        pageWidth = pageWidth,
        edgeContentPadding = edgeContentPadding,
        overlap = overlap,
    )
}
