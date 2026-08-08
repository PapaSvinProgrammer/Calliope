package com.mordva.domain.di

import com.mordva.domain.data.repository.RadioStationRepositoryImpl
import com.mordva.domain.data.service.RadioStationService
import com.mordva.domain.domain.repository.RadioStationRepository
import com.mordva.domain.domain.usecase.LoadRadioStationsUseCase
import com.mordva.domain.domain.usecase.SearchRadioStationsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val homeDomainModule = module {
    factoryOf(::LoadRadioStationsUseCase)
    factoryOf(::SearchRadioStationsUseCase)

    singleOf(::RadioStationRepositoryImpl) bind RadioStationRepository::class

    single<RadioStationService> {
        val retrofit = get<Retrofit>()
        retrofit.create<RadioStationService>()
    }
}