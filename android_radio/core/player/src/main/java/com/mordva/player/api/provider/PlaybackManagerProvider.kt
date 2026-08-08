package com.mordva.player.api.provider

import android.content.Context
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.player.api.PlaybackManager
import com.mordva.player.impl.Media3PlaybackManager
import kotlinx.coroutines.CoroutineScope

object PlaybackManagerProvider {
    fun provide(
        context: Context,
        scope: CoroutineScope,
        playbackDataStore: DataStore<PlaybackData>,
    ): PlaybackManager = Media3PlaybackManager(
        context = context,
        scope = scope,
        playbackDataStore = playbackDataStore,
    )
}