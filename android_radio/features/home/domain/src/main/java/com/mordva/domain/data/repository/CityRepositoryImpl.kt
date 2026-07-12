package com.mordva.domain.data.repository

import com.mordva.domain.data.mapper.toDomain
import com.mordva.domain.data.service.CityService
import com.mordva.domain.domain.repository.CityRepository
import com.mordva.domain.domain.model.City
import com.mordva.network.api.safeExecute

internal class CityRepositoryImpl(
    private val service: CityService,
) : CityRepository {
    override suspend fun getAll(
        page: Int,
        size: Int
    ): Result<List<City>> = safeExecute {
        service.getAll(
            page = page,
            size = size
        )
    }.map { it.content.toDomain() }

    override suspend fun searchByName(
        name: String,
        page: Int,
        size: Int
    ): Result<List<City>> = safeExecute {
        service.searchByName(
            name = name,
            page = page,
            size = size,
        )
    }.map { it.content.toDomain() }
}