package com.mordva.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mordva.system_ui.Resources

@Composable
internal fun CategorySuccessContent(
    categories: List<String>,
    selectedCategories: Set<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Resources.Dimens.DP8),
        verticalArrangement = Arrangement.spacedBy(Resources.Dimens.DP2),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Resources.Dimens.DP16),
    ) {
        categories.forEach { category ->
            val selected = category in selectedCategories
            FilterChip(
                selected = selected,
                onClick = { onItemClick(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
