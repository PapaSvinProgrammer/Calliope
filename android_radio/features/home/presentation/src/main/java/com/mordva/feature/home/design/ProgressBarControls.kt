package com.mordva.feature.home.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.mordva.feature.home.design.progressbar.CustomProgressBar
import com.mordva.feature.home.state.HomeScreenRadioState
import com.mordva.system_ui.R

@Composable
internal fun ProgressBarControls(
    state: HomeScreenRadioState,
    isExpanded: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
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
        HomeScreenRadioState.Error -> TODO()
        HomeScreenRadioState.Loading -> Text(text = "Zalupa")
        is HomeScreenRadioState.Success -> {
            CustomProgressBar(
                currentValue = state.currentValue,
                maxValue = state.maxValue,
                title = state.title,
                modifier = Modifier.weight(1f)
            )
        }
    }
}