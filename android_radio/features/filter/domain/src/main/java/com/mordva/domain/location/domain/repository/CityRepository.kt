package com.mordva.domain.location.domain.repository

import com.mordva.domain.location.domain.model.City

interface CityRepository {
    suspend fun getAll(
        page: Int = PAGE_DEFAULT,
        size: Int = SIZE_DEFAULT,
    ): Result<List<City>>

    suspend fun searchByName(
        name: String,
        page: Int = PAGE_DEFAULT,
        size: Int = SIZE_DEFAULT,
    ): Result<List<City>>

    private companion object {
        const val PAGE_DEFAULT = 0
        const val SIZE_DEFAULT = 20
    }
}