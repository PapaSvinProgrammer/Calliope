package com.mordva.radio.di

import com.mordva.network.api.AuthService
import com.mordva.network.api.TokenAuthenticator
import com.mordva.network.provider.OauthTokenProvider
import com.mordva.network.provider.OkHttpClientProvider
import com.mordva.network.provider.RetrofitClientProvider
import com.mordva.network.repository.AuthRepository
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val networkModule = module {
    singleOf(::RetrofitClientProvider)

    single<AuthService> {
        val client: OkHttpClient = OkHttpClient.Builder().build()
        val retrofit: Retrofit = get<RetrofitClientProvider>().provide(client)
        retrofit.create<AuthService>()
    }

    single {
        AuthRepository(
            authService = get(),
            tokenPreferencesRepository = get(),
        )
    }

    single {
        OauthTokenProvider(
            tokenPreferencesRepository = get(),
            coroutineScope = get(named(IO_SCOPE)),
        )
    }

    single {
        TokenAuthenticator(
            authRepository = get(),
            oauthTokenProvider = get(),
        )
    }

    single<OkHttpClient> {
        OkHttpClientProvider(
            oauthTokenProvider = get(),
            tokenAuthenticator = get(),
        ).provide()
    }

    single<Retrofit> {
        RetrofitClientProvider().provide(get())
    }
}
