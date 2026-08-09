package com.mordva.datastore.impl.provider

import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.datastore.api.repository.PlaybackPreferencesRepository
import com.mordva.datastore.impl.repository.PlaybackPreferencesRepositoryImpl

object PlaybackPreferencesProvider {
    fun provide(dataStore: DataStore<PlaybackData>): PlaybackPreferencesRepository {
        return PlaybackPreferencesRepositoryImpl(dataStore)
    }
}
