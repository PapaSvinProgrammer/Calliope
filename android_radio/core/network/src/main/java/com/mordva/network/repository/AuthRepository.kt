package com.mordva.network.repository

import com.mordva.datastore.api.repository.TokenPreferencesRepository
import com.mordva.network.api.AuthService

class AuthRepository(
    private val authService: AuthService,
    private val tokenPreferencesRepository: TokenPreferencesRepository,
) {
    suspend fun refreshToken(): Result<Unit> = runCatching {
        val response = authService.fetchToken()
        tokenPreferencesRepository.update(response.token)
    }
}
