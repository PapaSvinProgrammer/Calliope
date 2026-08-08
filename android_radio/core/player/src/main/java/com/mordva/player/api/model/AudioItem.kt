package com.mordva.player.api.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class AudioItem(
    val id: String,
    val uri: String,
    val title: String,
    val artist: String? = null,
    val artworkUri: String? = null,
)
