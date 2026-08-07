package com.mordva.datastore.impl.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.mordva.datastore.api.model.FilterData
import com.mordva.datastore.api.repository.FilterPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

internal class FilterPreferencesRepositoryImpl(
    private val dataStore: DataStore<FilterData>,
) : FilterPreferencesRepository {
    override fun get(): Flow<FilterData> = dataStore.data.catch { error ->
        Log.e(TAG, "Failed to read filter preferences", error)
        emit(FilterData())
    }

    override suspend fun update(filter: FilterData): Result<FilterData> = withContext(Dispatchers.IO) {
        runCatching { dataStore.updateData { filter } }
            .onFailure { Log.e(TAG, "Failed to update filter preferences", it) }
    }

    private companion object {
        const val TAG = "FilterPreferencesRepository"
    }
}
