package com.mordva.sdk.impl

import com.mordva.sdk.api.AuthSdk
import kotlinx.coroutines.flow.first

internal class AuthSdkImpl(
    private val authRepository: AuthRepository,
) : AuthSdk {
    override suspend fun getToken(): Result<String> {
        val token = authRepository.token.first()

        return if (token == null) {
            Result.failure(IllegalStateException("token = null"))
        } else {
            Result.success(token)
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        return authRepository.refreshToken()
    }
}