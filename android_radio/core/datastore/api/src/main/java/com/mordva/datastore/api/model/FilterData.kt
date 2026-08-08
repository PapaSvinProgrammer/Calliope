package com.mordva.datastore.api.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterData(
    val cities: List<CityData> = emptyList(),
)
