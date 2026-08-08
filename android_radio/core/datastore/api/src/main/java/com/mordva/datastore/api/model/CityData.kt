package com.mordva.datastore.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CityData(
    val id: Int = -1,
    val title: String = "",
    val regionTitle: String = "",
    val cityImageUrl: String? = null,
    val regionImageUrl: String? = null,
)