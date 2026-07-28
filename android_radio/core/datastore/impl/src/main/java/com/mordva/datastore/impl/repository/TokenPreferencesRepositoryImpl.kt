package com.mordva.datastore.impl.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mordva.datastore.api.manager.CryptoManager
import com.mordva.datastore.api.repository.TokenPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class TokenPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : TokenPreferencesRepository {
    override fun get(): Flow<String?> = dataStore.data
        .catch { Log.e(TAG, "Failed to read city preferences", it) }
        .map { prefs ->
            prefs[TOKEN_KEY]?.let { encryptedToken ->
                val res = cryptoManager.decrypt(encryptedToken)
                Log.d("RRRR", "decryptedToken = $res")
                res
            }
        }

    override suspend fun update(token: String): Result<Unit> = runCatching {
        Log.d("RRRR", "token = $token")
        dataStore.edit { prefs ->
            val encryptedToken = cryptoManager.encrypt(token)
            Log.d("RRRR", "encryptedToken = $encryptedToken")
            prefs[TOKEN_KEY] = encryptedToken
        }
    }

    override suspend fun clear(): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("encrypted_token")
        const val TAG = "TokenPreferencesRepositoryImpl"
    }
}