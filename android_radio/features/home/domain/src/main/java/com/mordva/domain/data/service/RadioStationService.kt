package com.mordva.domain.data.service

import com.mordva.domain.data.model.RadioStationDto
import com.mordva.network.api.Pager
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface RadioStationService {

    @GET("radio-stations")
    suspend fun getAll(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<Pager<RadioStationDto>>

    @GET("radio-stations/{id}")
    suspend fun getByStationId(
        @Path("id") id: Int,
    ): Response<RadioStationDto>

    @GET("city/{cityId}/radio-stations")
    suspend fun getByCityId(
        @Path("cityId") cityId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<Pager<RadioStationDto>>

    @GET("radio-stations/search")
    suspend fun searchByName(
        @Query("name") name: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<Pager<RadioStationDto>>
}