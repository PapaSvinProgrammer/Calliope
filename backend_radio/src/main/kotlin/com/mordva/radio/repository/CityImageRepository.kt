package com.mordva.radio.repository

import com.mordva.radio.entity.CityImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CityImageRepository : JpaRepository<CityImage, Int>
