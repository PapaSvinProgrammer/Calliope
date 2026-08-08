package com.mordva.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.model.getAvailableImages
import com.mordva.presentation.state.LocationCityState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationSuccessContent(
    selectedCityIds: Set<Int>,
    cityListState: LocationCityState.Success,
    lazyListState: LazyListState,
    onItemClick: (City) -> Unit,
) {
    Column {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = cityListState.items,
                key = { it.id },
            ) { city ->
                LocationListItem(
                    title = city.title,
                    images = city.getAvailableImages(),
                    isSelected = city.id in selectedCityIds,
                    onClick = { onItemClick(city) },
                )
            }
        }
    }
}