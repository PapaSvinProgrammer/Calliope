package com.mordva.control_bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.vectorResource

@Composable
internal fun SearchLayout(
    isExpanded: Boolean,
    searchText: String,
    onSearchClick: () -> Unit,
    onCloseClick: () -> Unit,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val buttonSize = dimensionResource(R.dimen.control_bar_button_size)
        val spacing = dimensionResource(R.dimen.control_bar_space_size)
        val animationDuration = integerResource(R.integer.control_button_animation_duration)
        val searchWidth = maxWidth - buttonSize - buttonSize - spacing

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape,
                )
            ) {
                IconButton(
                    onClick = onSearchClick,
                    enabled = !isExpanded,
                    modifier = Modifier.size(buttonSize)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = createExpandHorizontally(animationDuration),
                    exit = createShrinkHorizontally(animationDuration),
                ) {
                    SearchTextLayout(
                        text = searchText,
                        onTextChange = onTextChanged,
                        modifier = Modifier.requiredWidth(searchWidth),
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = createExpandHorizontally(animationDuration),
                exit = createShrinkHorizontally(animationDuration),
            ) {
                Spacer(modifier = Modifier.width(spacing))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = createExpandHorizontally(animationDuration),
                exit = createShrinkHorizontally(animationDuration),
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = CircleShape,
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

private fun createExpandHorizontally(duration: Int): EnterTransition = expandHorizontally(
    animationSpec = tween(durationMillis = duration),
    expandFrom = Alignment.Start,
)

private fun createShrinkHorizontally(duration: Int): ExitTransition = shrinkHorizontally(
    animationSpec = tween(durationMillis = duration),
    shrinkTowards = Alignment.Start,
)
