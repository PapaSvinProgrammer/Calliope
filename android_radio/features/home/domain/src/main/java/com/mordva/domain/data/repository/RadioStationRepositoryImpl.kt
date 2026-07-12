package com.mordva.domain.data.repository

import com.mordva.domain.data.mapper.toDomain
import com.mordva.domain.data.service.RadioStationService
import com.mordva.domain.domain.repository.RadioStationRepository
import com.mordva.domain.domain.model.RadioStation
import com.mordva.network.api.safeExecute

internal class RadioStationRepositoryImpl(
    private val service: RadioStationService,
) : RadioStationRepository {
    override suspend fun getAll(
        page: Int,
        size: Int
    ): Result<List<RadioStation>> = safeExecute {
        service.getAll(
            page = page,
            size = size,
        )
    }.map { it.content.toDomain() }

    override suspend fun getByStationId(stationId: Int): Result<RadioStation> = safeExecute {
        service.getByStationId(stationId)
    }.map { it.toDomain() }

    override suspend fun getByCityId(
        cityId: Int,
        page: Int,
        size: Int
    ): Result<List<RadioStation>> = safeExecute {
        service.getByCityId(
            cityId = cityId,
            page = page,
            size = size,
        )
    }.map { it.content.toDomain() }

    override suspend fun searchByName(
        name: String,
        page: Int,
        size: Int
    ): Result<List<RadioStation>> = safeExecute {
        service.searchByName(
            name = name,
            page = page,
            size = size,
        )
    }.map { it.content.toDomain() }
}
