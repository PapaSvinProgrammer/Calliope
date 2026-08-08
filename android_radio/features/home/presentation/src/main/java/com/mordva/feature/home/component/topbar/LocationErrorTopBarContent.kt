package com.mordva.feature.home.component.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mordva.feature.home.R
import com.mordva.system_ui.Resources

@Composable
internal fun LocationErrorTopBarContent(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                .padding(vertical = Resources.Dimens.DP5, horizontal = Resources.Dimens.DP10)
                .align(Alignment.Center),
        ) {
            Text(
                text = stringResource(R.string.title_location_error_top_bar),
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            )
        }
    }
}