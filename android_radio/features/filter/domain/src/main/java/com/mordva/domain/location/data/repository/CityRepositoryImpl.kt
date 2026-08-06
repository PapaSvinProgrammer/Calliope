package com.mordva.domain.location.data.repository

import com.mordva.domain.location.data.mapper.toDomain
import com.mordva.domain.location.data.service.CityService
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.repository.CityRepository
import com.mordva.network.api.safeExecute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CityRepositoryImpl(
    private val service: CityService,
) : CityRepository {
    override suspend fun getAll(
        page: Int, size: Int
    ): Result<List<City>> = withContext(Dispatchers.IO) {
        safeExecute {
            service.getAll(
                page = page,
                size = size,
            )
        }.map { it.content.toDomain() }
    }

    override suspend fun searchByName(
        name: String, page: Int, size: Int
    ): Result<List<City>> = withContext(Dispatchers.IO) {
        safeExecute {
            service.searchByName(
                name = name,
                page = page,
                size = size,
            )
        }.map { it.content.toDomain() }
    }
}