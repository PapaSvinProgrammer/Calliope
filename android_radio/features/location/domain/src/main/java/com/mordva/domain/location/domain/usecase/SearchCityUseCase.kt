package com.mordva.domain.location.domain.usecase

import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SearchCityUseCase(
    private val cityRepository: CityRepository,
) {
    private val mutex = Mutex()
    private var currentPage = 0
    private var lastQuery = ""

    suspend fun execute(q: String): Result<List<City>> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (q != lastQuery) {
                currentPage = 0
                lastQuery = q
            }

            val res = cityRepository.searchByName(
                name = q,
                page = currentPage,
            )

            res.onSuccess { currentPage++ }
        }
    }
}