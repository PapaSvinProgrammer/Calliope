package com.mordva.network.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface AuthService {
    @GET("auth/token")
    suspend fun fetchToken(): TokenResponse
}

@Serializable
data class TokenResponse(
    @SerialName(value = "token")
    val token: String,
)
