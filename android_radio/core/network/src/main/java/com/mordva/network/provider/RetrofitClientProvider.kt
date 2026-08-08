package com.mordva.network.provider

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitClientProvider {
    fun provide(okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val kotlinxConverterFactory = Json.asConverterFactory(contentType)

        return Retrofit.Builder()
            .baseUrl("https://mordva-calliope.ru/api/")
            .addConverterFactory(kotlinxConverterFactory)
            .client(okHttpClient)
            .build()
    }
}