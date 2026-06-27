package com.mordva.radio.service

import com.mordva.radio.dto.*
import com.mordva.radio.entity.RadioStation
import com.mordva.radio.repository.RadioStationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RadioStationService(
    private val radioStationRepository: RadioStationRepository
) {

    @Transactional(readOnly = true)
    fun getRadioStationById(id: Int): RadioStationDto? {
        val station = radioStationRepository.findByIdWithCities(id) ?: return null
        return station.toDto()
    }

    @Transactional(readOnly = true)
    fun getAllRadioStations(page: Int, size: Int): PageDto<RadioStationDto> {
        val pageable = PageRequest.of(page, size)
        val stationPage = radioStationRepository.findAll(pageable)
        return PageDto(
            content = stationPage.content.map { it.toDto() },
            page = stationPage.number,
            size = stationPage.size,
            totalElements = stationPage.totalElements,
            totalPages = stationPage.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getRadioStationsByCityId(cityId: Int, page: Int, size: Int): PageDto<RadioStationDto> {
        val pageable = PageRequest.of(page, size)
        val stationPage = radioStationRepository.findAllByCityId(cityId, pageable)
        return PageDto(
            content = stationPage.content.map { it.toDto() },
            page = stationPage.number,
            size = stationPage.size,
            totalElements = stationPage.totalElements,
            totalPages = stationPage.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun searchRadioStationsByName(name: String, page: Int, size: Int): PageDto<RadioStationDto> {
        val pageable = PageRequest.of(page, size)
        val stationPage = radioStationRepository.searchByName(name, pageable)
        return PageDto(
            content = stationPage.content.map { it.toDto() },
            page = stationPage.number,
            size = stationPage.size,
            totalElements = stationPage.totalElements,
            totalPages = stationPage.totalPages
        )
    }

    private fun RadioStation.toDto() = RadioStationDto(
        id = this.id!!,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        streamUrl = this.streamUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
