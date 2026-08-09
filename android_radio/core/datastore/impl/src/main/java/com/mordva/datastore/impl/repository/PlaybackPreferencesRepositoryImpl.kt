package com.mordva.datastore.impl.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.datastore.api.repository.PlaybackPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

internal class PlaybackPreferencesRepositoryImpl(
    private val dataStore: DataStore<PlaybackData>,
) : PlaybackPreferencesRepository {
    override fun get(): Flow<PlaybackData> = dataStore.data.catch { error ->
        Log.e(TAG, "Failed to read playback preferences", error)
        emit(PlaybackData())
    }

    override suspend fun update(playback: PlaybackData): Result<PlaybackData> =
        withContext(Dispatchers.IO) {
            runCatching { dataStore.updateData { playback } }
                .onFailure { Log.e(TAG, "Failed to update playback preferences", it) }
        }

    private companion object {
        const val TAG = "PlaybackPreferencesRepository"
    }
}
