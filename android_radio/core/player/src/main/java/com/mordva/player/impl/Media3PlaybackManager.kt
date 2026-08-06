package com.mordva.player.impl

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.mordva.player.api.AppMediaSessionService
import com.mordva.player.api.PlaybackManager
import com.mordva.player.api.model.AudioItem
import com.mordva.player.api.model.PlaybackState
import com.mordva.player.impl.utils.toMediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class Media3PlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope,
) : PlaybackManager {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(
            player: Player,
            events: Player.Events
        ) {
            Log.d(TAG, "onEvents = $events")
            updateState(player)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.d(TAG, "onPlayerError = $error")

            _state.update {
                it.copy(
                    isLoading = false,
                    error = error.localizedMessage ?: "Ошибка воспроизведения"
                )
            }
        }
    }

    override suspend fun connect() {
        Log.d(TAG, "connect(): controller = $controller")

        if (controller != null) return

        val existingFuture = controllerFuture

        if (existingFuture != null) {
            existingFuture.await()
            return
        }

        val token = SessionToken(
            context,
            ComponentName(
                context,
                AppMediaSessionService::class.java,
            )
        )

        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future

        runCatching {
            val newController = future.await()

            controller = newController
            newController.addListener(listener)

            _state.update { it.copy(isConnected = true) }

            updateState(newController)
            startProgressUpdates()
        }.onFailure { exception ->
            controllerFuture = null

            _state.update {
                it.copy(
                    isConnected = false,
                    error = exception.localizedMessage,
                )
            }
        }
    }

    override fun play(track: AudioItem) = executeWhenConnected { player ->
        player.setMediaItem(track.toMediaItem())
        player.prepare()
        player.play()
    }

    override fun playPlaylist(
        tracks: List<AudioItem>,
        startIndex: Int
    ) {
        if (tracks.isEmpty()) return

        executeWhenConnected { player ->
            val items = tracks.map(AudioItem::toMediaItem)
            val safeIndex = startIndex.coerceIn(items.indices)

            player.setMediaItems(
                items,
                safeIndex,
                C.TIME_UNSET
            )
            player.prepare()
            player.play()
        }
    }

    override fun play() = executeWhenConnected { it.play() }

    override fun pause() {
        controller?.pause()
    }

    override fun togglePlayPause() = executeWhenConnected { player ->
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    override fun seekForward() {
        controller?.seekForward()
    }

    override fun seekBack() {
        controller?.seekBack()
    }

    override fun next() {
        controller?.seekToNextMediaItem()
    }

    override fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    override fun stop() {
        controller?.apply {
            stop()
            clearMediaItems()
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun executeWhenConnected(
        action: (MediaController) -> Unit
    ) {
        controller?.let {
            Log.d(TAG, "executeWhenConnected(): action")
            action(it)
            return
        }

        scope.launch {
            connect()
            controller?.let(action)
        }
    }

    private fun updateState(player: Player) {
        Log.d(TAG, "updateState()")
        val metadata = player.mediaMetadata

        _state.update { oldState ->
            oldState.copy(
                isConnected = true,
                isPlaying = player.isPlaying,
                isLoading = player.playbackState == Player.STATE_BUFFERING,
                currentTrackId = player.currentMediaItem?.mediaId,
                title = metadata.title?.toString(),
                artist = metadata.artist?.toString(),
                artworkUri = metadata.artworkUri,
                positionMs = player.safePosition(),
                durationMs = player.safeDuration()
            )
        }
    }

    private fun startProgressUpdates() {
        Log.d(TAG, "startProgressUpdates()")
        progressJob?.cancel()

        progressJob = scope.launch {
            while (this.isActive) {
                controller?.let(::updateState)
                delay(500L.milliseconds)
            }
        }
    }

    private fun Player.safePosition(): Long {
        return currentPosition.coerceAtLeast(0L)
    }

    private fun Player.safeDuration(): Long {
        return duration.takeIf { it != C.TIME_UNSET && it >= 0L } ?: 0L
    }

    private companion object {
        const val TAG = "Media3PlaybackManager"
    }
}
