package com.mordva.network.provider

import com.mordva.datastore.api.repository.TokenPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope

class OauthTokenProvider(
    tokenPreferencesRepository: TokenPreferencesRepository,
    coroutineScope: CoroutineScope,
) {
    val token: StateFlow<String> = tokenPreferencesRepository
        .get()
        .map { it.orEmpty() }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = "",
        )
}
