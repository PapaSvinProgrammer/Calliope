package com.mordva.feature.home.di

import com.mordva.domain.di.homeDomainModule
import com.mordva.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val homeUiModule = module {
    viewModelOf(::HomeViewModel)
}

val homeModules = listOf(
    homeUiModule,
    homeDomainModule,
)
