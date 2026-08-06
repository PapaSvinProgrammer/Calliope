package com.mordva.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.mordva.system_ui.OverlappingImages
import com.mordva.system_ui.R
import com.mordva.system_ui.Resources

@Composable
internal fun LocationListItem(
    title: String,
    images: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    ListItem(
        onClick = onClick,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        leadingContent = {
            OverlappingImages(
                images = images,
                imageWidth = dimensionResource(R.dimen.icon_button_size),
                offset = dimensionResource(R.dimen.icon_button_size_offset),
            )
        },
        modifier = modifier.addBorderIfNeeded(isSelected),
    ) {
        Text(
            text = title,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        )
    }
}

@Composable
private fun Modifier.addBorderIfNeeded(isSelected: Boolean) = then(
    if (isSelected) {
        Modifier
            .padding(horizontal = 10.dp)
            .border(
                width = Resources.Dimens.DP1,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
            )
    } else {
        Modifier
    }
)