package com.mordva.domain.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CityDto(
    @SerialName(value = "id")
    val id: Int,
    @SerialName(value = "name")
    val title: String,
    @SerialName(value = "region")
    val regionTitle: String,
    @SerialName(value = "cityImage")
    val cityImage: ImageDto?,
    @SerialName(value = "regionImage")
    val regionImage: ImageDto?,
)