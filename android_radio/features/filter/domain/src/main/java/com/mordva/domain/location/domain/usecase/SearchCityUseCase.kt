package com.mordva.domain.location.domain.usecase

import com.mordva.connectivity.NetworkRequestRetryManager
import com.mordva.connectivity.RetriableRequest
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SearchCityUseCase(
    private val cityRepository: CityRepository,
    private val retryManager: NetworkRequestRetryManager,
) {
    private val mutex = Mutex()
    private var currentPage = 0
    private var lastQuery = ""

    suspend fun execute(q: String): RetriableRequest<List<City>> {
        if (q != lastQuery) {
            currentPage = 0
            lastQuery = q
        }

        return retryManager.execute(key = "$REQUEST_KEY:$q:$currentPage") {
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    cityRepository.searchByName(
                        name = q,
                        page = currentPage,
                    ).onSuccess { currentPage++ }
                }
            }
        }
    }

    private companion object {
        const val REQUEST_KEY = "cities:search"
    }
}
