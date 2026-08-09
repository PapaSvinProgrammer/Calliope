package com.mordva.domain.location.domain.usecase

import com.mordva.connectivity.NetworkRequestRetryManager
import com.mordva.connectivity.RetriableRequest
import com.mordva.domain.location.domain.model.City
import com.mordva.domain.location.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LoadCityUseCase(
    private val cityRepository: CityRepository,
    private val retryManager: NetworkRequestRetryManager,
) {
    private val mutex = Mutex()
    private var currentPage = 0

    suspend fun execute(
        size: Int = DEFAULT_SIZE,
    ): RetriableRequest<List<City>> = retryManager.execute(
        key = "$REQUEST_KEY:$currentPage:$size",
    ) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                cityRepository.getAll(
                    page = currentPage,
                    size = size,
                ).onSuccess { currentPage++ }
            }
        }
    }

    private companion object {
        const val DEFAULT_SIZE = 20
        const val REQUEST_KEY = "cities:load"
    }
}
