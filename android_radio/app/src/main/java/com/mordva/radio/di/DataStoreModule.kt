package com.mordva.radio.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mordva.datastore.api.datastore.cityDataStore
import com.mordva.datastore.api.datastore.tokenDataStore
import com.mordva.datastore.api.manager.CryptoManager
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.CityPreferencesRepository
import com.mordva.datastore.api.repository.TokenPreferencesRepository
import com.mordva.datastore.impl.provider.CityPreferencesProvider
import com.mordva.datastore.impl.provider.CryptoManagerProvider
import com.mordva.datastore.impl.provider.TokenPreferencesProvider
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val dataSoreModule = module {
    single<DataStore<CityData>>(Qualifiers.CITY_DATA_STORE) {
        get<Context>().cityDataStore
    }

    single<DataStore<Preferences>>(Qualifiers.TOKEN_DATA_STORE) {
        get<Context>().tokenDataStore
    }

    single<CryptoManager> {
        CryptoManagerProvider.provider(get())
    }

    single<CityPreferencesRepository> {
        CityPreferencesProvider.provide(
            dataStore = get(Qualifiers.CITY_DATA_STORE),
        )
    }

    single<TokenPreferencesRepository> {
        TokenPreferencesProvider.provide(
            dataStore = get(Qualifiers.TOKEN_DATA_STORE),
            cryptoManager = get(),
        )
    }
}

data object Qualifiers {
    val TOKEN_DATA_STORE = qualifier("token_data_store")
    val CITY_DATA_STORE = qualifier("city_data_store")
}