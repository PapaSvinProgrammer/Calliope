package com.mordva.datastore.impl.provider

import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.FilterData
import com.mordva.datastore.api.repository.FilterPreferencesRepository
import com.mordva.datastore.impl.repository.FilterPreferencesRepositoryImpl

object FilterPreferencesProvider {
    fun provide(dataStore: DataStore<FilterData>): FilterPreferencesRepository {
        return FilterPreferencesRepositoryImpl(dataStore)
    }
}
