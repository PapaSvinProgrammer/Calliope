package com.mordva.network.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pager<T>(
    @SerialName(value = "content")
    val content: List<T>,
    @SerialName(value = "page")
    val page: Int,
    @SerialName(value = "size")
    val size: Int,
    @SerialName(value = "totalElements")
    val totalElements: Int,
    @SerialName(value = "totalPages")
    val totalPages: Int,
)