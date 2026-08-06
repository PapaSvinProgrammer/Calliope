package com.mordva.radio.di

import com.mordva.player.api.PlaybackManager
import com.mordva.player.api.provider.PlaybackManagerProvider
import com.mordva.radio.domain.MainViewModel
import com.mordva.sdk.api.AuthSdk
import com.mordva.sdk.api.AuthSdkConfig
import com.mordva.sdk.api.AuthSdkProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val APP_SCOPE = "APP_COROUTINE_SCOPE"
const val IO_SCOPE = "IO_COROUTINE_SCOPE"
const val DEFAULT_SCOPE = "DEFAULT_COROUTINE_SCOPE"

val appModule = module {
    viewModelOf(::MainViewModel)

    single<AuthSdk> {
        val config = AuthSdkConfig(
            context = get(),
        )

        AuthSdkProvider.provide(config)
    }

    single<PlaybackManager> {
        PlaybackManagerProvider.provide(
            context = get(),
            scope = get(named(APP_SCOPE)),
        )
    }
}

val coroutineModule = module {
    single<CoroutineScope>(named(APP_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    single<CoroutineScope>(named(IO_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    single<CoroutineScope>(named(DEFAULT_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}