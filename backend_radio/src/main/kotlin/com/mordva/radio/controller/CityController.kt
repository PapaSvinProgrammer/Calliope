package com.mordva.radio.controller

import com.mordva.radio.dto.*
import com.mordva.radio.service.CityService
import com.mordva.radio.service.RadioStationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/city")
class CityController(
    private val cityService: CityService,
    private val radioStationService: RadioStationService
) {

    @GetMapping
    fun getAllCities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageDto<CityDto>> {
        val cities = cityService.getAllCities(page, size)
        return ResponseEntity.ok(cities)
    }

    @GetMapping("/{cityId}/radio-stations")
    fun getRadioStationsByCity(
        @PathVariable cityId: Int,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageDto<RadioStationDto>> {
        val stations = radioStationService.getRadioStationsByCityId(cityId, page, size)
        return ResponseEntity.ok(stations)
    }

    @GetMapping("/search")
    fun searchCitiesByName(
        @RequestParam name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageDto<CityDto>> {
        val cities = cityService.searchCitiesByName(name, page, size)
        return ResponseEntity.ok(cities)
    }
}
