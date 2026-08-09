package com.mordva.player.api.provider

import android.content.Context
import com.mordva.datastore.api.repository.PlaybackPreferencesRepository
import com.mordva.player.api.PlaybackManager
import com.mordva.player.impl.Media3PlaybackManager
import kotlinx.coroutines.CoroutineScope

object PlaybackManagerProvider {
    fun provide(
        context: Context,
        scope: CoroutineScope,
        playbackPreferencesRepository: PlaybackPreferencesRepository,
    ): PlaybackManager = Media3PlaybackManager(
        context = context,
        scope = scope,
        playbackPreferencesRepository = playbackPreferencesRepository,
    )
}