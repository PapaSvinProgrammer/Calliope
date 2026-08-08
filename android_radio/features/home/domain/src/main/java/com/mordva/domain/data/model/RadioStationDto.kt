package com.mordva.domain.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RadioStationDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("imageUrl")
    val imageUrl: String,
    @SerialName("imageWidth")
    val imageWidth: Int? = null,
    @SerialName("imageHeight")
    val imageHeight: Int? = null,
    @SerialName("streamUrl")
    val streamUrl: String,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String,
)