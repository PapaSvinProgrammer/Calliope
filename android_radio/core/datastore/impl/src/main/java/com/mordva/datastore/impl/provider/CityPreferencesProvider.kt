package com.mordva.datastore.impl.provider

import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.CityPreferencesRepository
import com.mordva.datastore.impl.repository.CityPreferencesRepositoryImpl

object CityPreferencesProvider {
    fun provide(dataStore: DataStore<CityData>): CityPreferencesRepository {
        return CityPreferencesRepositoryImpl(dataStore)
    }
}