package com.mordva.domain.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImageDto(
    @SerialName(value = "id")
    val id: Int,
    @SerialName(value = "imageUrl")
    val imageUrl: String,
    @SerialName(value = "downloadUrl")
    val downloadUrl: String, // TODO: Удалить совсем из БД
    @SerialName(value = "source")
    val sourceUrl: String, // TODO: Удалить совсем из БД
)