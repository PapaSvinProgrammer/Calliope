package com.mordva.domain.domain.repository

import com.mordva.domain.domain.model.RadioStation

interface RadioStationRepository {
    suspend fun getAll(
        page: Int = PAGE_DEFAULT,
        size: Int = SIZE_DEFAULT,
    ): Result<List<RadioStation>>

    suspend fun getByStationId(stationId: Int): Result<RadioStation>

    suspend fun getByCityId(
        cityId: Int,
        page: Int = PAGE_DEFAULT,
        size: Int = SIZE_DEFAULT,
    ): Result<List<RadioStation>>

    suspend fun searchByName(
        name: String,
        page: Int = PAGE_DEFAULT,
        size: Int = SIZE_DEFAULT,
    ): Result<List<RadioStation>>

    private companion object {
        const val PAGE_DEFAULT = 0
        const val SIZE_DEFAULT = 20
    }
}