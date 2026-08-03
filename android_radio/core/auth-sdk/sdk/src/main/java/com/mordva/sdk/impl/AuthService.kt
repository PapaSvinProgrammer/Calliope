package com.mordva.sdk.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

internal interface AuthService {
    @GET("auth/token")
    suspend fun fetchToken(): TokenResponse
}

@Serializable
data class TokenResponse(
    @SerialName(value = "token")
    val token: String,
)
