package com.mordva.domain.domain.model

data class RadioStation(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val streamUrl: String,
)