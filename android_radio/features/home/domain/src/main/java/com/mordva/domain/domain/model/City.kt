package com.mordva.domain.domain.model

data class City(
    val id: Int,
    val title: String,
    val regionTitle: String,
    val cityImageUrl: String?,
    val regionImageUrl: String?,
)