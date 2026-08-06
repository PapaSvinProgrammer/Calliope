package com.mordva.presentation.state

sealed interface FilterType {
    data object Location : FilterType
    data object Category : FilterType
}