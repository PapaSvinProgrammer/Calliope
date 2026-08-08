package com.mordva.radio.di

import com.mordva.network.provider.OkHttpClientProvider
import com.mordva.network.provider.RetrofitClientProvider
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    singleOf(::RetrofitClientProvider)

    single<OkHttpClient> {
        OkHttpClientProvider(
            authSdk = get(),
        ).provide()
    }

    single<Retrofit> {
        RetrofitClientProvider().provide(get())
    }
}
