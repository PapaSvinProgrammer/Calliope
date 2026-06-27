package com.mordva.radio.service

import com.mordva.radio.dto.*
import com.mordva.radio.entity.City
import com.mordva.radio.entity.CityImage
import com.mordva.radio.entity.RegionImage
import com.mordva.radio.repository.CityRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CityService(
    private val cityRepository: CityRepository
) {

    @Transactional(readOnly = true)
    fun getAllCities(page: Int, size: Int): PageDto<CityDto> {
        val pageable = PageRequest.of(page, size)
        val cityPage = cityRepository.findAll(pageable)
        return PageDto(
            content = cityPage.content.map { it.toDto() },
            page = cityPage.number,
            size = cityPage.size,
            totalElements = cityPage.totalElements,
            totalPages = cityPage.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun searchCitiesByName(name: String, page: Int, size: Int): PageDto<CityDto> {
        val pageable = PageRequest.of(page, size)
        val cityPage = cityRepository.searchByName(name, pageable)
        return PageDto(
            content = cityPage.content.map { it.toDto() },
            page = cityPage.number,
            size = cityPage.size,
            totalElements = cityPage.totalElements,
            totalPages = cityPage.totalPages
        )
    }

    private fun City.toDto() = CityDto(
        id = this.id!!,
        name = this.name,
        region = this.region,
        cityImage = this.cityImage?.toImageDto(),
        regionImage = this.regionImage?.toImageDto()
    )

    private fun CityImage.toImageDto() = ImageDto(
        id = this.id!!,
        imageUrl = this.imageUrl,
        downloadUrl = this.downloadUrl,
        source = this.source
    )

    private fun RegionImage.toImageDto() = ImageDto(
        id = this.id!!,
        imageUrl = this.imageUrl,
        downloadUrl = this.downloadUrl,
        source = this.source
    )
}
