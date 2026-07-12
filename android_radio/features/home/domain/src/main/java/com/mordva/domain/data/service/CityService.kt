package com.mordva.domain.data.service

import com.mordva.domain.data.model.CityDto
import com.mordva.network.api.Pager
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface CityService {

    @GET("city")
    suspend fun getAll(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<Pager<CityDto>>

    @GET("search")
    suspend fun searchByName(
        @Query("name") name: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<Pager<CityDto>>
}