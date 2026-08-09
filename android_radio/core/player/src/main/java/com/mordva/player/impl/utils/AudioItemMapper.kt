package com.mordva.player.impl.utils

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.player.api.model.AudioItem

internal fun AudioItem.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(artworkUri?.let(Uri::parse))
                .build()
        )
        .build()
}

internal fun AudioItem.toPlaybackData() = PlaybackData(
    id = id,
    uri = uri,
    title = title,
    artist = artist,
    artworkUri = artworkUri,
)