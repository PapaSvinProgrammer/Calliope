package com.mordva.domain.domain.usecase

import com.mordva.connectivity.NetworkRequestRetryManager
import com.mordva.connectivity.RetriableRequest
import com.mordva.domain.domain.model.RadioStation
import com.mordva.domain.domain.repository.RadioStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadRadioStationsUseCase(
    private val radioStationRepository: RadioStationRepository,
    private val retryManager: NetworkRequestRetryManager,
) {
    private var currentPage = 0

    suspend fun execute(size: Int): RetriableRequest<List<RadioStation>> =
        retryManager.execute(key = "$REQUEST_KEY:$currentPage:$size") {
            withContext(Dispatchers.IO) {
                radioStationRepository.getAll(
                    page = currentPage,
                    size = size,
                ).onSuccess { currentPage++ }
            }
        }

    private companion object {
        const val REQUEST_KEY = "radio-stations:load"
    }
}
