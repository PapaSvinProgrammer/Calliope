package com.mordva.player.impl.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.mordva.player.api.model.AudioItem

internal fun AudioItem.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(artworkUri)
                .build()
        )
        .build()
}