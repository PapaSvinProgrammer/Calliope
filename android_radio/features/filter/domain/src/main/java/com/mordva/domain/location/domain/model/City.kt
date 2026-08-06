package com.mordva.domain.location.domain.model

data class City(
    val id: Int,
    val title: String,
    val regionTitle: String?,
    val cityImageUrl: String?,
    val regionImageUrl: String?,
)

fun City.getAvailableImages(): List<String> = listOfNotNull(regionImageUrl, cityImageUrl)
