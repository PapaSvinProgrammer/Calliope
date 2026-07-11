package com.mordva.control_bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp

@Composable
internal fun SearchLayout(
    isExpanded: Boolean,
    searchText: String,
    searchTextWidth: Dp,
    buttonSize: Dp,
    spacing: Dp,
    onSearchClick: () -> Unit,
    onCloseClick: () -> Unit,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animationDuration = integerResource(R.integer.control_button_animation_duration)

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
                    modifier = Modifier.width(searchTextWidth)
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
                modifier = Modifier
                    .size(buttonSize)
                    .background(
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

private fun createExpandHorizontally(duration: Int): EnterTransition = expandHorizontally(
    animationSpec = tween(durationMillis = duration),
    expandFrom = Alignment.Start,
)

private fun createShrinkHorizontally(duration: Int): ExitTransition = shrinkHorizontally(
    animationSpec = tween(durationMillis = duration),
    shrinkTowards = Alignment.Start,
)
