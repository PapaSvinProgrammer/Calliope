package com.mordva.feature.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.mordva.feature.home.R
import com.mordva.feature.home.component.progressbar.CustomProgressBar
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.system_ui.Resources
import com.mordva.system_ui.shimmer.shimmer

@Composable
internal fun ProgressBarControls(
    state: HomeScreenRadioState,
    isExpanded: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(
            visible = !isExpanded && state is HomeScreenRadioState.Success,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
        ) {
            IconButton(onClick = onPauseClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_pause),
                    contentDescription = null,
                )
            }
        }

        RenderCustomProgressBar(state)
    }
}

@Composable
private fun RowScope.RenderCustomProgressBar(
    state: HomeScreenRadioState,
) {
    when (state) {
        HomeScreenRadioState.Error -> {
            CustomProgressBar(
                currentValue = 0f,
                maxValue = 1f,
                title = stringResource(R.string.error_progress_bar_title),
                modifier = Modifier.weight(1f),
            )
        }

        HomeScreenRadioState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.progress_bar_shimmer_height))
                    .clip(CircleShape)
                    .shimmer()
                    .padding(
                        vertical = Resources.Dimens.DP12,
                        horizontal = Resources.Dimens.DP24,
                    ),
            )
        }

        is HomeScreenRadioState.Success -> {
            CustomProgressBar(
                currentValue = state.currentValue,
                maxValue = state.maxValue,
                title = state.station.title,
                modifier = Modifier.weight(1f),
            )
        }
    }
}