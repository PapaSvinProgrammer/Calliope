package com.mordva.feature.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import com.mordva.feature.home.R
import com.mordva.feature.home.state.HomeScreenAction
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.feature.home.state.HomeScreenState
import com.mordva.system_ui.Resources
import com.mordva.system_ui.image.ImageSquareWithShadow
import com.mordva.system_ui.shimmer.shimmer

@Composable
internal fun BodyContent(
    uiState: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            HomeImageContent(
                radioState = uiState.radioState,
                isExpanded = uiState.isPlayRadio,
                onPlayClick = { onAction(HomeScreenAction.OnPlayClick) },
            )
        }

        ProgressBarControls(
            state = uiState.radioState,
            isExpanded = !uiState.isPlayRadio,
            onPauseClick = { onAction(HomeScreenAction.OnPlayClick) },
            modifier = Modifier.padding(horizontal = Resources.Dimens.DP16)
        )

        Spacer(modifier = Modifier.height(Resources.Dimens.DP30))
    }
}

@Composable
private fun HomeImageContent(
    radioState: HomeScreenRadioState,
    isExpanded: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isExpanded,
        ) { isExpanded ->
            when (radioState) {
                HomeScreenRadioState.Error,
                HomeScreenRadioState.Loading,
                    -> {
                    Box(
                        modifier = modifier
                            .size(dimensionResource(R.dimen.home_radio_image_size))
                            .clip(RoundedCornerShape(Resources.Dimens.DP10))
                            .shimmer()
                    )
                }

                is HomeScreenRadioState.Success -> {
                    RenderHomeImageContent(
                        imageUrl = radioState.artworkUrl.orEmpty(),
                        isExpanded = isExpanded,
                        onPlayClick = onPlayClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderHomeImageContent(
    imageUrl: String?,
    isExpanded: Boolean,
    onPlayClick: () -> Unit,
) {
    if (isExpanded) {
        ImageSquareWithShadow(
            model = imageUrl,
            modifier = Modifier.size(dimensionResource(R.dimen.home_radio_image_size))
        )
    } else {
        PlayButton(onClick = onPlayClick)
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_play_circle),
        contentDescription = null,
        modifier = modifier
            .size(dimensionResource(R.dimen.play_button_size))
            .alpha(0.7f)
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onClick,
            ),
    )
}