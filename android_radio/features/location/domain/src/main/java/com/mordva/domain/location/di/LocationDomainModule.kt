package com.mordva.domain.location.di

import com.mordva.domain.location.data.repository.CityRepositoryImpl
import com.mordva.domain.location.data.service.CityService
import com.mordva.domain.location.domain.repository.CityRepository
import com.mordva.domain.location.domain.usecase.LoadCityUseCase
import com.mordva.domain.location.domain.usecase.SearchCityUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val locationDomainModule = module {
    factoryOf(::SearchCityUseCase)
    factoryOf(::LoadCityUseCase)

    singleOf(::CityRepositoryImpl) bind CityRepository::class

    single<CityService> {
        val retrofit = get<Retrofit>()
        retrofit.create<CityService>()
    }
}