package com.mordva.datastore.impl

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mordva.datastore.api.CryptoManager
import com.mordva.datastore.api.TokenPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class TokenPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : TokenPreferencesRepository {
    override fun get(): Flow<String?> = dataStore.data
        .catch { Log.e(TAG, "Failed to read city preferences", it) }
        .map { prefs ->
            prefs[TOKEN_KEY]?.let { encryptedToken ->
                val res = cryptoManager.decrypt(encryptedToken)
                res
            }
        }

    override suspend fun update(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        updateEncryptedToken(token)
    }

    private suspend fun updateEncryptedToken(token: String): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            val encryptedToken = cryptoManager.encrypt(token)
            prefs[TOKEN_KEY] = encryptedToken
        }
    }

    override suspend fun clear(): Result<Unit> = withContext(Dispatchers.IO) {
        clearEncryptedToken()
    }

    private suspend fun clearEncryptedToken(): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("encrypted_token")
        const val TAG = "TokenPreferencesRepositoryImpl"
    }
}