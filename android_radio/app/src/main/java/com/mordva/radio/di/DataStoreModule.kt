package com.mordva.radio.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.datastore.cityDataStore
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.CityPreferencesRepository
import com.mordva.datastore.impl.provider.CityPreferencesProvider
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val dataSoreModule = module {
    single<DataStore<CityData>>(Qualifiers.CITY_DATA_STORE) {
        get<Context>().cityDataStore
    }

    single<CityPreferencesRepository> {
        CityPreferencesProvider.provide(
            dataStore = get(Qualifiers.CITY_DATA_STORE),
        )
    }
}

data object Qualifiers {
    val CITY_DATA_STORE = qualifier("city_data_store")
}