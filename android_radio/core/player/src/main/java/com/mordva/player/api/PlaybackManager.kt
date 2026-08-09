package com.mordva.player.api

import com.mordva.player.api.model.AudioItem
import kotlinx.coroutines.flow.StateFlow
import com.mordva.player.api.model.PlaybackState

interface PlaybackManager {

    val state: StateFlow<PlaybackState>

    val size: Int

    suspend fun connect()

    fun play(track: AudioItem)

    fun prepare(track: AudioItem)

    fun setPlaylist(tracks: List<AudioItem>)

    fun playAt(index: Int)

    fun play()

    fun pause()

    fun togglePlayPause()

    fun seekTo(positionMs: Long)

    fun seekForward()

    fun seekBack()

    fun next()

    fun previous()

    fun stop()

    fun clearError()
}
