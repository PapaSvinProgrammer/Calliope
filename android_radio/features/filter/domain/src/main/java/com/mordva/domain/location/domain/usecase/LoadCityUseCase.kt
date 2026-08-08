package com.mordva.domain.location.domain.usecase

import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LoadCityUseCase(
    private val cityRepository: CityRepository,
) {
    private val mutex = Mutex()
    private var currentPage = 0

    suspend fun execute(
        size: Int = DEFAULT_SIZE,
    ): Result<List<City>> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val res = cityRepository.getAll(
                page = currentPage,
                size = size,
            )

            res.onSuccess { currentPage++ }
        }
    }

    private companion object {
        const val DEFAULT_SIZE = 20
    }
}