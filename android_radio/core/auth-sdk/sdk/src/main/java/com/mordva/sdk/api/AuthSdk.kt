package com.mordva.sdk.api

interface AuthSdk {
    suspend fun getToken(): Result<String>
    suspend fun refreshToken(): Result<Unit>
}