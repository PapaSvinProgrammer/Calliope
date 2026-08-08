package com.mordva.player.api.model

import android.net.Uri

data class PlaybackState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentTrackId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: Uri? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)
