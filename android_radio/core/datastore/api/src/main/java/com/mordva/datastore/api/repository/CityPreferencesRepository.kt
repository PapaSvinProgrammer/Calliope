package com.mordva.datastore.api.repository

import com.mordva.datastore.api.model.CityData
import kotlinx.coroutines.flow.Flow

interface CityPreferencesRepository {
    fun get(): Flow<CityData>
    suspend fun update(city: CityData): Result<CityData>
}