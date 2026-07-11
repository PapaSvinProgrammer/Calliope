package com.mordva.control_bar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp

@Composable
fun ControlBottomBar(
    modifier: Modifier = Modifier,
    searchExpanded: Boolean,
    searchText: String,
    selectedItem: ControlItem,
    action: (BottomControlsAction) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val extraSize = dimensionResource(R.dimen.controls_layout_vertical_padding) * 2
        val buttonSize = dimensionResource(R.dimen.control_bar_button_size) + extraSize
        val spacing = dimensionResource(R.dimen.control_bar_space_size)
        val searchTextWidth = maxWidth - buttonSize - buttonSize - spacing

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlsLayout(
                selectedItem = selectedItem,
                onClick = { action(BottomControlsAction.Control.OnItemClick(it)) },
                internalPadding = PaddingValues(
                    vertical = dimensionResource(R.dimen.controls_layout_vertical_padding)
                ),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(1f)
                    .clipToBounds(),
            )

            SearchLayout(
                isExpanded = searchExpanded,
                searchText = searchText,
                buttonSize = buttonSize,
                spacing = spacing,
                searchTextWidth = searchTextWidth,
                onSearchClick = { action(BottomControlsAction.Search.OnSearchClick) },
                onCloseClick = { action(BottomControlsAction.Search.OnCloseClick) },
                onTextChanged = { action(BottomControlsAction.Search.OnTextChanged(it)) },
            )
        }
    }
}
