package com.mordva.radio.repository

import com.mordva.radio.entity.RegionImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegionImageRepository : JpaRepository<RegionImage, Int>
