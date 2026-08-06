package com.mordva.datastore.impl.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.repository.CityPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

internal class CityPreferencesRepositoryImpl(
    private val dataStore: DataStore<CityData>,
) : CityPreferencesRepository {

    override fun get(): Flow<CityData> = dataStore.data
        .catch { error ->
            Log.e(TAG, "Failed to read city preferences", error)
            emit(CityData())
        }

    override suspend fun update(city: CityData): Result<CityData> = withContext(Dispatchers.IO) {
        runCatching {
            dataStore.updateData { city }
        }.onFailure { error ->
            Log.e(TAG, "Failed to update city preferences", error)
        }
    }

    private companion object {
        const val TAG = "CityPreferencesRepositoryImpl"
    }
}
