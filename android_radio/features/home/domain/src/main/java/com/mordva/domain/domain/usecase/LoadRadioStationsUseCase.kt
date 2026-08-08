package com.mordva.domain.domain.usecase

import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.repository.RadioStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadRadioStationsUseCase(
    private val radioStationRepository: RadioStationRepository,
) {
    private var currentPage = 0

    suspend fun execute(size: Int): Result<List<RadioStation>> = withContext(Dispatchers.IO) {
        val res = radioStationRepository.getAll(
            page = currentPage,
            size = size
        )

        res.onSuccess { currentPage++ }

        return@withContext res
    }
}