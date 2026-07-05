package com.mordva.control_bar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp

@Composable
fun ControlBottomBar() {
    var searchState by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val buttonSize = dimensionResource(R.dimen.control_bar_button_size)
        val spacing = dimensionResource(R.dimen.control_bar_space_size)
        val searchTextWidth = maxWidth - buttonSize - buttonSize - spacing

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlsLayout(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(1f)
                    .clipToBounds()
            )

            SearchLayout(
                isExpanded = searchState,
                searchText = searchText,
                searchTextWidth = searchTextWidth,
                onSearchClick = { searchState = !searchState },
                onCloseClick = {
                    if (searchText.isEmpty()) {
                        searchState = !searchState
                    }

                    searchText = ""
                },
                onTextChanged = { searchText = it }
            )
        }
    }
}
