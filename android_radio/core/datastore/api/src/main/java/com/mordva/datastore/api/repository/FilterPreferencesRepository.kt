package com.mordva.datastore.api.repository

import com.mordva.datastore.api.model.FilterData
import kotlinx.coroutines.flow.Flow

interface FilterPreferencesRepository {
    fun get(): Flow<FilterData>
    suspend fun update(filter: FilterData): Result<FilterData>
}
