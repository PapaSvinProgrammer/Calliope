package com.mordva.datastore.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaybackData(
    val id: String = "",
    val uri: String = "",
    val title: String = "",
    val artist: String? = null,
    val artworkUri: String? = null,
)
