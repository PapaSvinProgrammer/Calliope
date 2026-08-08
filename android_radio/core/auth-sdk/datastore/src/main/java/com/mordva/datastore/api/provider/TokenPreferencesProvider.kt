package com.mordva.datastore.api.provider

import android.content.Context
import com.mordva.datastore.api.TokenPreferencesRepository
import com.mordva.datastore.api.tokenDataStore
import com.mordva.datastore.impl.TokenPreferencesRepositoryImpl

object TokenPreferencesProvider {
    fun provide(context: Context): TokenPreferencesRepository {
        val applicationContext = context.applicationContext
        return TokenPreferencesRepositoryImpl(
            dataStore = applicationContext.tokenDataStore,
            cryptoManager = CryptoManagerProvider.provider(applicationContext),
        )
    }
}