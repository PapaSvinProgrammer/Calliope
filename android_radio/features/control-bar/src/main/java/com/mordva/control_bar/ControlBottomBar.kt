package com.mordva.control_bar

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp

@Composable
fun ControlBottomBar() {
    var searchState by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        SearchLayout(
            modifier = Modifier.align(Alignment.CenterEnd),
            searchText = searchText,
            isExpanded = searchState,
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