package com.mordva.radio.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.datastore.filterDataStore
import com.mordva.datastore.api.datastore.playbackDataStore
import com.mordva.datastore.api.model.FilterData
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.datastore.api.repository.FilterPreferencesRepository
import com.mordva.datastore.api.repository.PlaybackPreferencesRepository
import com.mordva.datastore.impl.provider.FilterPreferencesProvider
import com.mordva.datastore.impl.provider.PlaybackPreferencesProvider
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val dataSoreModule = module {
    single<DataStore<FilterData>>(Qualifiers.FILTER_DATA_STORE) {
        get<Context>().filterDataStore
    }

    single<FilterPreferencesRepository> {
        FilterPreferencesProvider.provide(
            dataStore = get(Qualifiers.FILTER_DATA_STORE),
        )
    }

    single<DataStore<PlaybackData>>(Qualifiers.PLAYBACK_DATA_STORE) {
        get<Context>().playbackDataStore
    }

    single<PlaybackPreferencesRepository> {
        PlaybackPreferencesProvider.provide(
            dataStore = get(Qualifiers.PLAYBACK_DATA_STORE),
        )
    }
}

data object Qualifiers {
    val FILTER_DATA_STORE = qualifier("filter_data_store")
    val PLAYBACK_DATA_STORE = qualifier("playback_data_store")
}