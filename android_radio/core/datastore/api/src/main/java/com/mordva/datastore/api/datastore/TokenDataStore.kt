package com.mordva.datastore.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(TokenDataStoreConfig.NAME)

private object TokenDataStoreConfig {
    const val NAME = "token_preferences"
}