package com.mordva.datastore.api.repository

import com.mordva.datastore.api.model.PlaybackData
import kotlinx.coroutines.flow.Flow

interface PlaybackPreferencesRepository {
    fun get(): Flow<PlaybackData>
    suspend fun update(playback: PlaybackData): Result<PlaybackData>
}
