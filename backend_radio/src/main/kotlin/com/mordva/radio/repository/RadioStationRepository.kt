package com.mordva.radio.repository

import com.mordva.radio.entity.RadioStation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RadioStationRepository : JpaRepository<RadioStation, Int> {

    @Query("SELECT rs FROM RadioStation rs LEFT JOIN FETCH rs.cities WHERE rs.id = :id")
    fun findByIdWithCities(@Param("id") id: Int): RadioStation?

    @Query("""
        SELECT DISTINCT rs FROM RadioStation rs
        JOIN rs.cities c
        WHERE c.id = :cityId
    """)
    fun findAllByCityId(@Param("cityId") cityId: Int, pageable: Pageable): Page<RadioStation>

    @Query("SELECT rs FROM RadioStation rs WHERE LOWER(rs.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(@Param("name") name: String, pageable: Pageable): Page<RadioStation>
}
