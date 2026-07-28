package com.mordva.datastore.api.repository

import kotlinx.coroutines.flow.Flow

interface TokenPreferencesRepository {
    fun get(): Flow<String?>
    suspend fun update(token: String): Result<Unit>
    suspend fun clear(): Result<Unit>
}