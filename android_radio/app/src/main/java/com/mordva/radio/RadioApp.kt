package com.mordva.radio

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.mordva.feature.home.di.homeModules
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
                homeModules,
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }
}
