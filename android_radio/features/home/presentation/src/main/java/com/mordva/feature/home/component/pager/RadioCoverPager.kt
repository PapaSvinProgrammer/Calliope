package com.mordva.feature.home.component.pager

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.system_ui.Resources
import kotlin.math.absoluteValue

@Composable
internal fun RadioCoverPager(
    pagerState: PagerState,
    items: List<HomeScreenRadioState>,
    onClickPagerItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

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
                    RadioPagerItem(
                        id = item.station.id,
                        imageUrl = item.station.imageUrl,
                        title = item.station.title,
                        isSelected = item.isSelected,
                        pageOffset = pageOffset,
                        onClick = { onClickPagerItem(page) },
                        modifier = Modifier.radioItemParams(pageOffset),
                    )
                }
            }
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
