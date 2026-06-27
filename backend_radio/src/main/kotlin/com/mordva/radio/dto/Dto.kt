package com.mordva.radio.dto

import java.time.OffsetDateTime

data class RadioStationDto(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val streamUrl: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class RadioStationBriefDto(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val streamUrl: String
)

data class CityDto(
    val id: Int,
    val name: String,
    val region: String?,
    val cityImage: ImageDto?,
    val regionImage: ImageDto?
)

data class ImageDto(
    val id: Int,
    val imageUrl: String,
    val downloadUrl: String,
    val source: String
)

data class PageDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
