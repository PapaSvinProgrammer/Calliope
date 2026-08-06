package com.mordva.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mordva.presentation.state.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterTabs(
    selectedType: FilterType,
    onTypeClick: (FilterType) -> Unit,
) {
    val tabs = listOf(
        FilterType.Location to "Location",
        FilterType.Category to "Categories",
    )

    PrimaryTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedType },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        tabs.forEach { (type, title) ->
            Tab(
                selected = type == selectedType,
                onClick = { onTypeClick(type) },
                text = { Text(title) },
            )
        }
    }
}