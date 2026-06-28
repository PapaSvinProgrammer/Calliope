package com.mordva.network.provider

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClientProvider {
    private val retrofitClient by lazy { createRetrofit() }

    fun provide(): Retrofit = retrofitClient

    private fun createRetrofit(): Retrofit {
        val contentType = "application/json".toMediaType()
        val kotlinxConverterFactory = Json.asConverterFactory(contentType)

        return Retrofit.Builder()
            .addConverterFactory(kotlinxConverterFactory)
            .client(OkHttpClientProvider.provide())
            .build()
    }
}