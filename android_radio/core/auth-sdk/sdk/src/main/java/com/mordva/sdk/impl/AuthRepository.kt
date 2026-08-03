package com.mordva.sdk.impl

import com.mordva.datastore.api.TokenPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class AuthRepository(
    private val authService: AuthService,
    private val tokenPreferencesRepository: TokenPreferencesRepository,
) {
    val token: StateFlow<String?> = tokenPreferencesRepository
        .get()
        .map { it }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = "",
        )

    suspend fun refreshToken(): Result<Unit> = runCatching {
        val response = authService.fetchToken()
        tokenPreferencesRepository.update(response.token)
    }
}