package com.mordva.domain.domain.usecase

import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.repository.RadioStationRepository

class SearchRadioStationsUseCase(
    private val radioStationRepository: RadioStationRepository,
) {
    private var currentPage = 0
    private var lastQuery = ""

    suspend fun execute(
        query: String,
        size: Int = DEFAULT_PAGE_SIZE,
    ): Result<List<RadioStation>> {
        if (query != lastQuery) {
            currentPage = 0
            lastQuery = query
        }

        val result = radioStationRepository.searchByName(
            name = query,
            page = currentPage,
            size = size,
        )

        result.onSuccess { currentPage++ }
        return result
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}