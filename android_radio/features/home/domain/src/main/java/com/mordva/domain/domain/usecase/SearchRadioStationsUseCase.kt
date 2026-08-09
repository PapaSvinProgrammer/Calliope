package com.mordva.domain.domain.usecase

import com.mordva.connectivity.NetworkRequestRetryManager
import com.mordva.connectivity.RetriableRequest
import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.repository.RadioStationRepository

class SearchRadioStationsUseCase(
    private val radioStationRepository: RadioStationRepository,
    private val retryManager: NetworkRequestRetryManager,
) {
    private var currentPage = 0
    private var lastQuery = ""

    suspend fun execute(
        query: String,
        size: Int = DEFAULT_PAGE_SIZE,
    ): RetriableRequest<List<RadioStation>> {
        if (query != lastQuery) {
            currentPage = 0
            lastQuery = query
        }

        return retryManager.execute(key = "$REQUEST_KEY:$query:$currentPage:$size") {
            radioStationRepository.searchByName(
                name = query,
                page = currentPage,
                size = size,
            ).onSuccess { currentPage++ }
        }
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val REQUEST_KEY = "radio-stations:search"
    }
}
