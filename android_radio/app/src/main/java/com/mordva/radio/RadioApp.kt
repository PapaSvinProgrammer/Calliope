package com.mordva.radio

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.mordva.feature.home.di.homeModules
import com.mordva.feature.search.presentation.di.searchModules
import com.mordva.presentation.di.filterModules
import com.mordva.radio.di.appModule
import com.mordva.radio.di.coroutineModule
import com.mordva.radio.di.dataSoreModule
import com.mordva.radio.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RadioApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RadioApp)

            modules(
                appModule,
                dataSoreModule,
                coroutineModule,
                networkModule,
            )

            modules(
                homeModules + filterModules + searchModules
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val imageHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return ImageLoader.Builder(this)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient }))
            }
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}
