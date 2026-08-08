package com.mordva.feature.search.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.mordva.domain.domain.model.RadioStation
import androidx.compose.ui.unit.dp
import com.mordva.system_ui.Resources

@Composable
internal fun StationListItem(
    station: RadioStation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
) {
    ListItem(
        onClick = onClick,
        content = {
            Text(
                text = station.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
        },
        supportingContent = station
            .description
            .takeIf(String::isNotBlank)
            ?.let { description ->
                {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                    )
                }
            },
        leadingContent = {
            AsyncImage(
                model = station.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(Resources.Dimens.DP12)),
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        modifier = modifier.then(
            if (isPlaying) {
                Modifier
                    .padding(horizontal = Resources.Dimens.DP16)
                    .border(
                        width = Resources.Dimens.DP1,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium,
                    )
            } else {
                Modifier
            },
        ),
    )
}
