package com.mordva.radio.repository

import com.mordva.radio.entity.City
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CityRepository : JpaRepository<City, Int> {

    @Query("SELECT c FROM City c LEFT JOIN FETCH c.cityImage LEFT JOIN FETCH c.regionImage LEFT JOIN FETCH c.radioStations")
    fun findAllWithDetails(): List<City>

    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(@Param("name") name: String, pageable: Pageable): Page<City>
}
