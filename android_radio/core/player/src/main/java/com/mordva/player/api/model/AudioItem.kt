package com.mordva.player.api.model

import android.net.Uri

data class AudioItem(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String? = null,
    val artworkUri: Uri? = null
)
