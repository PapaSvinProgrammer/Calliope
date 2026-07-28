package com.mordva.datastore.impl.provider

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mordva.datastore.api.manager.CryptoManager
import com.mordva.datastore.api.repository.TokenPreferencesRepository
import com.mordva.datastore.impl.repository.TokenPreferencesRepositoryImpl

object TokenPreferencesProvider {
    fun provide(
        dataStore: DataStore<Preferences>,
        cryptoManager: CryptoManager,
    ): TokenPreferencesRepository {
        return TokenPreferencesRepositoryImpl(
            dataStore = dataStore,
            cryptoManager = cryptoManager,
        )
    }
}