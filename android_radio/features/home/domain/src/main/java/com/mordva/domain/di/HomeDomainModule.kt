package com.mordva.domain.di

import com.mordva.domain.data.repository.CityRepositoryImpl
import com.mordva.domain.data.repository.RadioStationRepositoryImpl
import com.mordva.domain.data.service.CityService
import com.mordva.domain.data.service.RadioStationService
import com.mordva.domain.domain.repository.CityRepository
import com.mordva.domain.domain.repository.RadioStationRepository
import com.mordva.domain.domain.usecase.LoadRadioStationsUseCase
import com.mordva.network.provider.RetrofitClientProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val homeDomainModule = module {
    factoryOf(::LoadRadioStationsUseCase)

    singleOf(::RadioStationRepositoryImpl) bind RadioStationRepository::class
    singleOf(::CityRepositoryImpl) bind CityRepository::class

    single<Retrofit> {
        RetrofitClientProvider.provide()
    }

    single<RadioStationService> {
        val retrofit = get<Retrofit>()
        retrofit.create<RadioStationService>()
    }

    single<CityService> {
        val retrofit = get<Retrofit>()
        retrofit.create<CityService>()
    }
}