package com.mordva.radio.controller

import com.mordva.radio.dto.*
import com.mordva.radio.service.RadioStationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/radio-stations")
class RadioStationController(
    private val radioStationService: RadioStationService
) {

    @GetMapping
    fun getAllRadioStations(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageDto<RadioStationDto>> {
        val stations = radioStationService.getAllRadioStations(page, size)
        return ResponseEntity.ok(stations)
    }

    @GetMapping("/{id}")
    fun getRadioStationById(@PathVariable id: Int): ResponseEntity<RadioStationDto> {
        val station = radioStationService.getRadioStationById(id)
        return if (station != null) {
            ResponseEntity.ok(station)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    fun searchRadioStationsByName(
        @RequestParam name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageDto<RadioStationDto>> {
        val stations = radioStationService.searchRadioStationsByName(name, page, size)
        return ResponseEntity.ok(stations)
    }
}
